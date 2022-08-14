package org.moddingx.modlistcreator.platform;

import com.google.gson.*;
import org.moddingx.modlistcreator.Main;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public record ModrinthModpack(
        String title,
        Modpack.Minecraft minecraft,
        String version,
        List<Modpack.File> files
) implements Modpack {

    public static final int FORMAT_VERSION = 1;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static Optional<ModrinthModpack> load(JsonElement json) throws IOException {
        if (!json.isJsonObject() || !json.getAsJsonObject().has("formatVersion") || json.getAsJsonObject().get("formatVersion").getAsInt() != FORMAT_VERSION) {
            return Optional.empty();
        }
        JsonObject obj = json.getAsJsonObject();
        String title = Objects.requireNonNull(obj.get("name"), "Missing property: name").getAsString();
        String version = Objects.requireNonNull(obj.get("versionId"), "Missing property: version").getAsString();
        
        JsonObject deps = Objects.requireNonNull(obj.get("dependencies"), "Missing property: dependencies").getAsJsonObject();
        String mcVersion = Objects.requireNonNull(deps.get("minecraft"), "Missing property: dependencies.minecraft").getAsString();
        
        if (deps.size() != 2) throw new JsonSyntaxException("Modpack must specify exactly one loader dependency alog side its minecraft dependency");
        String loader = null;
        String loaderVersion = null;
        for (String key : deps.keySet()) {
            if ("minecraft".equals(key)) continue;
            loader = key.endsWith("-loader") ? key.substring(0, key.length() - 7) : key;
            loaderVersion = deps.get(key).getAsString();
        }
        
        JsonArray filesArray = Objects.requireNonNull(obj.get("files"), "Missing property: files").getAsJsonArray();
        Set<String> hashes = new HashSet<>();
        for (int i = 0; i < filesArray.size(); i++) {
            JsonObject fileObj = filesArray.get(i).getAsJsonObject();
            JsonObject hashesObj = Objects.requireNonNull(fileObj.get("hashes"), "Missing property: files[" + i + "].hashes").getAsJsonObject();
            String sha512 = Objects.requireNonNull(hashesObj.get("sha512"), "Missing property: files[" + i + "].hashes.sha512").getAsString();
            hashes.add(sha512);
        }
        
        JsonObject requestData = new JsonObject();
        requestData.addProperty("algorithm", "sha512");
        JsonArray hashesArray = new JsonArray();
        hashes.forEach(hashesArray::add);
        requestData.add("hashes", hashesArray);
       
        List<File> files = new ArrayList<>();
        try {
            JsonObject filesResponse = makeRequest(HttpRequest.newBuilder()
                    .uri(URI.create("https://api.modrinth.com/v2/version_files"))
                    .POST(HttpRequest.BodyPublishers.ofString(Main.GSON.toJson(requestData), StandardCharsets.UTF_8))
                    .header("Content-Type", "application/json")
            ).getAsJsonObject();
            
            record FileData(String projectId, String versionId, String fileName) {}
            List<FileData> fileData = new ArrayList<>();

            for (String hash : hashes) {
                if (!filesResponse.has(hash)) {
                    throw new IllegalArgumentException("File not hosted on modrinth: sha512=" + hash);
                }

                JsonObject versionData = filesResponse.get(hash).getAsJsonObject();
                String fileName = null;
                JsonArray versionFiles = versionData.get("files").getAsJsonArray();
                if (versionFiles.size() == 1) {
                    fileName = versionFiles.get(0).getAsJsonObject().get("filename").getAsString();
                } else {
                    for (JsonElement versionFile : versionFiles) {
                        if (versionFile.getAsJsonObject().get("primary").getAsBoolean()) {
                            fileName = versionFile.getAsJsonObject().get("filename").getAsString();
                            break;
                        }
                    }
                }

                if (fileName == null) {
                    throw new IOException("Version has no primary file");
                }
                fileData.add(new FileData(versionData.get("project_id").getAsString(), versionData.get("id").getAsString(), fileName));
            }
            
            JsonArray allProjectIds = new JsonArray();
            fileData.stream().map(FileData::projectId).distinct().forEach(allProjectIds::add);
            JsonArray projectsResponse = makeRequest(HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.modrinth.com/v2/projects?ids=" + URLEncoder.encode(Main.GSON.toJson(allProjectIds), StandardCharsets.UTF_8)))
            ).getAsJsonArray();

            record ProjectData(String slug, String name, URI website, String teamId) {}
            Map<String, ProjectData> projectData = new HashMap<>();
            for (JsonElement entry : projectsResponse) {
                JsonObject projectEntry = entry.getAsJsonObject();
                projectData.put(projectEntry.get("id").getAsString(), new ProjectData(
                        projectEntry.get("slug").getAsString(),
                        projectEntry.get("title").getAsString(),
                        URI.create("https://modrinth.com/" + URLEncoder.encode(projectEntry.get("project_type").getAsString(), StandardCharsets.UTF_8) + "/" + URLEncoder.encode(projectEntry.get("slug").getAsString(), StandardCharsets.UTF_8)),
                        projectEntry.get("team").getAsString()
                ));
            }
            
            JsonArray allTeamIds = new JsonArray();
            projectData.values().stream().map(ProjectData::teamId).distinct().forEach(allTeamIds::add);
            JsonArray teamsResponse = makeRequest(HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.modrinth.com/v2/teams?ids=" + URLEncoder.encode(Main.GSON.toJson(allTeamIds), StandardCharsets.UTF_8)))
            ).getAsJsonArray();
            
            record TeamData(String owner, URI teamURL) {}
            Map<String, TeamData> teamData = new HashMap<>();
            for (JsonElement entryArr : teamsResponse) {
                for (JsonElement entry : entryArr.getAsJsonArray()) {
                    JsonObject teamEntry = entry.getAsJsonObject();
                    if ("Owner".equals(teamEntry.get("role").getAsString())) {
                        JsonObject user = teamEntry.get("user").getAsJsonObject();
                        String name = user.get("username").getAsString();
                        if (user.has("name") && !user.get("name").isJsonNull() && !user.get("name").getAsString().isEmpty()) {
                            name = user.get("name").getAsString();
                        }
                        teamData.put(teamEntry.get("team_id").getAsString(), new TeamData(
                                name, URI.create("https://modrinth.com/user/" + URLEncoder.encode(user.get("username").getAsString(), StandardCharsets.UTF_8))
                        ));
                    }
                }
            }
            
            for (FileData fd : fileData) {
                ProjectData pd = projectData.get(fd.projectId());
                if (pd == null) throw new IOException("Project not resolved: " + fd.projectId());
                TeamData td = teamData.get(pd.teamId());
                if (td == null) throw new IOException("Team not resolved: " + pd.teamId() + " (of project " + pd.slug() + ")");
                files.add(new DefaultFile(pd.slug(), pd.name(), fd.fileName(), td.owner(), pd.website(), URI.create(pd.website() + "/").resolve("version/" + fd.versionId()), td.teamURL()));
            }
        } catch (JsonParseException e) {
            throw new IOException("Failed to query modrinth api", e);
        }
        
        return Optional.of(new ModrinthModpack(title, new Modpack.Minecraft(mcVersion, loader, loaderVersion), version, List.copyOf(files)));
    }

    private static JsonElement makeRequest(HttpRequest.Builder builder) throws IOException {
        HttpRequest request = builder
                .header("Accept", "application/json")
                .header("User-Agent", "ModdingX/ModListCreator")
                .build();
        try {
            String response = CLIENT.send(request, resp -> {
                if ((resp.statusCode() / 100) == 2 && resp.statusCode() != 204) {
                    return HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
                } else {
                    return HttpResponse.BodySubscribers.replacing("\0" + resp.statusCode());
                }
            }).body();
            if (response.startsWith("\0")) {
                throw new IOException("HTTP " + response.substring(1));
            } else {
                try {
                    return Main.GSON.fromJson(response, JsonElement.class);
                } catch (JsonParseException e) {
                    throw new IOException("Invalid jso nresponse from modrinth api: " + response, e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted");
        }
    }
}

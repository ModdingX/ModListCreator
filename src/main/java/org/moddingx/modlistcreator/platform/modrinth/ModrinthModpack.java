package org.moddingx.modlistcreator.platform.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.moddingx.modlistcreator.ModListCreator;
import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.types.FileBase;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModrinthModpack extends Modpack {

    public static final int FORMAT_VERSION = 1;

    private JsonObject json;
    private List<ProjectEntry> files;

    @Override
    public int formatVersion() {
        return FORMAT_VERSION;
    }

    @Override
    public String title() {
        return this.json.get("name").getAsString();
    }

    @Override
    public Minecraft minecraft() {
        JsonObject deps = this.json.get("dependencies").getAsJsonObject();
        return new Minecraft(deps.get("minecraft").getAsString(), this.modLoader(deps));
    }

    @Override
    public String version() {
        return this.json.get("versionId").getAsString();
    }

    @Override
    public PackType packType() {
        return PackType.MODRINTH;
    }

    @Override
    public Modpack load(JsonObject json) {
        this.json = json;
        return this;
    }

    @Override
    public List<ProjectEntry> files() {
        if (this.files == null) {
            JsonArray array = this.json.get("files").getAsJsonArray();
            Set<String> hashes = IntStream.range(0, array.size())
                    .mapToObj(i -> array.get(i).getAsJsonObject())
                    .map(json -> json.get("hashes").getAsJsonObject())
                    .map(json -> json.get("sha512").getAsString())
                    .collect(Collectors.toSet());
            List<ProjectEntry> projects = this.retrieveFiles(hashes);
            projects.sort(Comparator.comparing(o -> o.projectName().toLowerCase(Locale.ROOT)));

            this.files = List.copyOf(projects);
        }

        return this.files;
    }

    @Override
    public String authorLink(String author) {
        return "https://modrinth.com/user/" + author;
    }

    private List<ProjectEntry> retrieveFiles(Set<String> hashes) {
        JsonArray array = new JsonArray();
        hashes.forEach(array::add);

        JsonObject data = new JsonObject();
        data.add("hashes", array);
        data.addProperty("algorithm", "sha512");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/version_files"))
                .header("Content-Type", "application/json")
                .header("User-Agent", "ModdingX/ModListCreator")
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = ModListCreator.GSON.fromJson(response.body(), JsonObject.class);

            int i = 0;
            final int total = hashes.size();

            Map<String, JsonObject> fileInfoMap = new HashMap<>();
            for (String hash : hashes) {
                JsonObject value = json.get(hash).getAsJsonObject();
                String projectId = value.get("project_id").getAsString();
                fileInfoMap.put(projectId, value);
            }

            Map<String, JsonObject> projectInfoMap = this.retrieveProjectMap(fileInfoMap.keySet());
            Map<String, String> authorsMap = this.retrieveAuthors(projectInfoMap.values().stream().map(info -> info.get("team").getAsString()).collect(Collectors.toSet()));

            List<ProjectEntry> files = new ArrayList<>();
            for (String hash : hashes) {
                JsonObject project = json.get(hash).getAsJsonObject();
                String projectId = project.get("project_id").getAsString();
                JsonObject projectJson = projectInfoMap.get(projectId).getAsJsonObject();
                String teamId = projectJson.get("team").getAsString();
                ProjectEntry e = new ProjectEntry(projectJson.get("title").getAsString(), projectId, authorsMap.get(teamId), URI.create("https://modrinth.com/mod/" + projectJson.get("slug").getAsString()), project.get("id").getAsString());
                files.add(e);

                String progress = String.format("%1$" + (String.valueOf(total).length() * 2 + 1) + "s", ++i + "/" + total).replace(' ', '0') + " ";
                FileBase.log(this.title(), progress + "\u001B[33m" + e.projectName() + "\u001B[0m found");
            }

            return files;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to retrieve project information for '" + this.title() + "'");
        }
    }

    private Map<String, JsonObject> retrieveProjectMap(Set<String> projectIds) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/projects?ids=" + projectIds.stream().collect(Collectors.joining("%22,%22", "[%22", "%22]"))))
                .header("Content-Type", "application/json")
                .header("User-Agent", "ModdingX/ModListCreator")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonArray array = ModListCreator.GSON.fromJson(response.body(), JsonArray.class);

            Map<String, JsonObject> map = new HashMap<>();
            for (JsonElement element : array) {
                map.put(element.getAsJsonObject().get("id").getAsString(), element.getAsJsonObject());
            }

            return Map.copyOf(map);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> retrieveAuthors(Set<String> teamIds) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/teams?ids=" + teamIds.stream().collect(Collectors.joining("%22,%22", "[%22", "%22]"))))
                .header("Content-Type", "application/json")
                .header("User-Agent", "ModdingX/ModListCreator")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonArray array = ModListCreator.GSON.fromJson(response.body(), JsonArray.class);
            Map<String, String> map = new HashMap<>();
            teamIds.forEach(id -> {
                outer:
                for (JsonElement element : array) {
                    for (JsonElement team : element.getAsJsonArray()) {
                        if (team.getAsJsonObject().get("team_id").getAsString().equals(id)) {
                            JsonObject user = team.getAsJsonObject().get("user").getAsJsonObject();
                            if (team.getAsJsonObject().get("role").getAsString().equals("Owner")) {
                                map.put(id, user.get("username").getAsString());
                                break outer;
                            }
                        }
                    }

                }
            });

            return Collections.unmodifiableMap(map);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to retrieve authors information for '" + this.title() + "'");
        }
    }

    private ModLoader modLoader(JsonObject deps) {
        if (deps.has("forge")) {
            return new ModLoader("forge", deps.get("forge").getAsString());
        }

        if (deps.has("fabric-loader")) {
            return new ModLoader("fabric-loader", deps.get("fabric-loader").getAsString());
        }

        if (deps.has("quilt-loader")) {
            return new ModLoader("quilt-loader", deps.get("quilt-loader").getAsString());
        }

        throw new IllegalStateException("Unknown launcher for pack '" + this.title() + "'");
    }
}

package org.moddingx.modlistcreator.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.moddingx.cursewrapper.api.CurseWrapper;
import org.moddingx.cursewrapper.api.response.FileInfo;
import org.moddingx.cursewrapper.api.response.ProjectInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public record CurseModpack(
        String title,
        Modpack.Minecraft minecraft,
        String version,
        List<Modpack.File> files
) implements Modpack {

    public static final int MANIFEST_VERSION = 1;
    private static final CurseWrapper API = new CurseWrapper(URI.create("https://curse.melanx.de/"));


    public static Optional<CurseModpack> load(JsonElement json) throws IOException {
        if (!json.isJsonObject() || !json.getAsJsonObject().has("manifestVersion") || json.getAsJsonObject().get("manifestVersion").getAsInt() != MANIFEST_VERSION) {
            return Optional.empty();
        }
        JsonObject obj = json.getAsJsonObject();
        String title = Objects.requireNonNull(obj.get("name"), "Missing property: name").getAsString();
        String version = obj.has("version") ? obj.get("version").getAsString() : "unknown";
        
        JsonObject minecraft = Objects.requireNonNull(obj.get("minecraft"), "Missing property: minecraft").getAsJsonObject();
        String mcVersion = Objects.requireNonNull(minecraft.get("version"), "Missing property: minecraft.version").getAsString();
        JsonArray loaderArray = Objects.requireNonNull(minecraft.get("modLoaders"), "Missing property: minecraft.modLoaders").getAsJsonArray();
        if (loaderArray.size() != 1) throw new JsonSyntaxException("Modpack must define exactly one mod loader");
        String loaderId = Objects.requireNonNull(loaderArray.get(0).getAsJsonObject().get("id"), "Missing property: minecraft.modLoaders[0].id").getAsString();
        if (!loaderId.contains("-")) throw new JsonSyntaxException("Modpack loader id is invalid: " + loaderId);
        
        JsonArray filesArray = Objects.requireNonNull(obj.get("files"), "Missing property: files").getAsJsonArray();
        Set<Integer> projectIds = new HashSet<>();
        Map<Integer, Integer> fileIds = new HashMap<>();
        for (int i = 0; i < filesArray.size(); i++) {
            JsonObject fileObj = filesArray.get(i).getAsJsonObject();
            int projectId = Objects.requireNonNull(fileObj.get("projectID"), "Missing property: files[" + i + "].projectID").getAsInt();
            int fileId = Objects.requireNonNull(fileObj.get("fileID"), "Missing property: files[" + i + "].fileID").getAsInt();
            projectIds.add(projectId);
            fileIds.put(projectId, fileId);
        }
        Map<Integer, ProjectInfo> resolvedProjects = API.getProjects(projectIds);
        if (projectIds.stream().anyMatch(id -> !resolvedProjects.containsKey(id))) {
            throw new IllegalStateException("Not all projects could be resolved.");
        }
        List<Modpack.File> files = fileIds.entrySet().stream()
                .<Modpack.File>map(entry -> new CurseFile(resolvedProjects.get(entry.getKey()), entry.getValue()))
                .toList();
        
        return Optional.of(new CurseModpack(title, new Modpack.Minecraft(
                mcVersion, loaderId.substring(0, loaderId.indexOf('-')), loaderId.substring(loaderId.indexOf('-') + 1)
        ), version, List.copyOf(files)));
    }
    
    // Don't resolve file name and URL if not needed
    private static class CurseFile implements Modpack.File {

        private final ProjectInfo project;
        private final int fileId;
        private FileInfo file;

        private CurseFile(ProjectInfo project, int fileId) {
            this.project = project;
            this.fileId = fileId;
            this.file = null;
        }

        @Override
        public String projectSlug() {
            return this.project.slug();
        }

        @Override
        public String projectName() {
            return this.project.name();
        }

        @Override
        public String fileName() {
            if (this.file == null) {
                try {
                    this.file = API.getFile(this.project.projectId(), this.fileId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return this.file.name();
        }

        @Override
        public String author() {
            return this.project.owner();
        }

        @Override
        public URI projectWebsite() {
            return this.project.website();
        }

        @Override
        public URI fileWebsite() {
            return URI.create(this.projectWebsite() + "/").resolve("files/" + this.fileId);
        }

        @Override
        public URI authorWebsite() {
            return URI.create("https://www.curseforge.com/members/" + URLEncoder.encode(this.author(), StandardCharsets.UTF_8) + "/projects");
        }
    }
}

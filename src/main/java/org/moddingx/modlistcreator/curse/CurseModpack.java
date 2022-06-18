package org.moddingx.modlistcreator.curse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.moddingx.cursewrapper.api.CurseWrapper;
import org.moddingx.cursewrapper.api.response.FileInfo;
import org.moddingx.cursewrapper.api.response.ProjectInfo;
import org.moddingx.modlistcreator.ModListCreator;
import org.moddingx.modlistcreator.types.FileBase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CurseModpack {

    private final Minecraft minecraft;
    @SuppressWarnings("FieldCanBeLocal")
    private final String manifestType = "minecraftModpack";
    @SuppressWarnings("FieldCanBeLocal")
    private final String overrides = "overrides";
    @SuppressWarnings("FieldCanBeLocal")
    private final int manifestVersion = 1;
    private final String version;
    private final String name;
    private final List<ProjectEntry> files = new ArrayList<>();

    private CurseModpack(JsonElement element) throws IOException, NullPointerException {
        CurseWrapper wrapper = ModListCreator.getWrapper();
        JsonObject json = element.getAsJsonObject();
        JsonObject minecraft = (JsonObject) json.get("minecraft");
        String version = minecraft.get("version").getAsString();
        Set<ModLoader> loaders = new HashSet<>();
        for (JsonElement modLoader : minecraft.get("modLoaders").getAsJsonArray()) {
            JsonObject loader = modLoader.getAsJsonObject();
            loaders.add(new ModLoader(loader.get("id").getAsString(), loader.get("primary").getAsBoolean()));
        }
        this.minecraft = new Minecraft(version, loaders);
        if (json.has("version")) {
            this.version = json.get("version").getAsString();
        } else {
            this.version = "undefined version";
        }
        this.name = json.get("name").getAsString();

        Set<Integer> projectIds = new HashSet<>();
        json.get("files").getAsJsonArray().forEach(file -> {
            projectIds.add(file.getAsJsonObject().get("projectID").getAsInt());
        });
        Map <Integer, ProjectInfo> projectInfoMap = wrapper.getProjects(projectIds);

        int i = 0;
        final Integer total = json.get("files").getAsJsonArray().size();

        for (JsonElement fileElement : json.get("files").getAsJsonArray()) {
            JsonObject file = fileElement.getAsJsonObject();
            FileInfo fileInfo = wrapper.getFile(file.get("projectID").getAsInt(), file.get("fileID").getAsInt());
            ProjectEntry e = new ProjectEntry(fileInfo, projectInfoMap.get(file.get("projectID").getAsInt()));
            this.files.add(e);

            // ' 001/354 '
            String progress = String.format("%1$" + (String.valueOf(total).length() * 2 + 1) + "s", ++i + "/" + total).replace(' ', '0') + " ";

            FileBase.log(this.name, progress + "\u001B[33m" + (e.getProject().name()) + "\u001B[0m found");
        }
        this.files.sort(Comparator.comparing(o -> o.getProject().name().toLowerCase(Locale.ROOT)));
    }

    public static CurseModpack fromManifest(File file) {
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            JsonElement jsonElement = JsonParser.parseReader(reader);
            return new CurseModpack(jsonElement);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public String getManifestType() {
        return this.manifestType;
    }

    public String getOverrides() {
        return this.overrides;
    }

    public int getManifestVersion() {
        return this.manifestVersion;
    }

    public String getVersion() {
        return this.version;
    }

    public String getName() {
        return this.name;
    }

    public List<ProjectEntry> getFiles() {
        return this.files;
    }

    public static class ProjectEntry {

        private final FileInfo file;
        private final ProjectInfo project;

        @Deprecated
        public ProjectEntry(FileInfo file) throws IOException {
            this.file = file;
            this.project = ModListCreator.getWrapper().getProject(file.projectId());
        }

        public ProjectEntry(FileInfo file, ProjectInfo project) {
            this.file = file;
            this.project = project;
        }

        public FileInfo getFile() {
            return this.file;
        }

        public ProjectInfo getProject() {
            return this.project;
        }

        @Override
        public String toString() {
            return this.project.name() + "(File: " + this.file.name() + ")";
        }
    }

    private record Minecraft(String version, Set<ModLoader> loaders) {
    }

    private record ModLoader(String id, boolean primary) {
    }
}

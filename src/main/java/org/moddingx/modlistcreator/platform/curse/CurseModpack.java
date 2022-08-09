package org.moddingx.modlistcreator.platform.curse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.moddingx.cursewrapper.api.CurseWrapper;
import org.moddingx.cursewrapper.api.response.FileInfo;
import org.moddingx.cursewrapper.api.response.ProjectInfo;
import org.moddingx.modlistcreator.ModListCreator;
import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.types.FileBase;

import java.io.IOException;
import java.util.*;

public class CurseModpack extends Modpack {

    public static final int FORMAT_VERSION = 1;

    private JsonObject json;
    private List<Modpack.ProjectEntry> files;

    @Override
    public int formatVersion() {
        return FORMAT_VERSION;
    }

    @Override
    public String title() {
        return this.json.get("name").getAsString();
    }

    @Override
    public Modpack.Minecraft minecraft() {
        JsonObject minecraft = this.json.get("minecraft").getAsJsonObject();
        String version = minecraft.get("version").getAsString();
        JsonObject loader = minecraft.get("modLoaders").getAsJsonArray().get(0).getAsJsonObject();
        Modpack.ModLoader mainLoader = new Modpack.ModLoader(loader.get("id").getAsString().split("-")[0], loader.get("id").getAsString().split("-")[1]);

        return new Modpack.Minecraft(version, mainLoader);
    }

    @Override
    public String version() {
        if (this.json.has("version")) {
            return this.json.get("version").getAsString();
        } else {
            return "undefined version";
        }
    }

    @Override
    public PackType packType() {
        return PackType.CURSEFORGE;
    }

    @Override
    public Modpack load(JsonObject json) {
        this.json = json;
        return this;
    }

    @Override
    public List<Modpack.ProjectEntry> files() {
        if (this.files == null) {

            List<ProjectEntry> projects = this.retrieveFiles(new HashMap<>(), 1);
            projects.sort(Comparator.comparing(o -> o.projectName().toLowerCase(Locale.ROOT)));

            this.files = List.copyOf(projects);
        }

        return this.files;
    }

    @Override
    public String authorLink(String author) {
        return "https://www.curseforge.com/members/" + author + "/projects";
    }

    private List<ProjectEntry> retrieveFiles(Map<Integer, ProjectEntry> map, int step) {
        List<ProjectEntry> files = new ArrayList<>(map.values());
        CurseWrapper wrapper = ModListCreator.getWrapper();

        try {
            Set<Integer> projectIds = new HashSet<>();
            this.json.get("files").getAsJsonArray().forEach(file -> {
                projectIds.add(file.getAsJsonObject().get("projectID").getAsInt());
            });
            Map<Integer, ProjectInfo> projectInfoMap = wrapper.getProjects(projectIds);

            int i = files.size();
            final int total = projectIds.size();

            for (JsonElement fileElement : this.json.get("files").getAsJsonArray()) {
                JsonObject file = fileElement.getAsJsonObject();
                int projectId = file.get("projectID").getAsInt();
                if (map.containsKey(projectId)) {
                    continue;
                }

                FileInfo fileInfo = wrapper.getFile(projectId, file.get("fileID").getAsInt());
                ProjectInfo projectInfo = projectInfoMap.get(projectId);
                Modpack.ProjectEntry e = new Modpack.ProjectEntry(projectInfo.name(), fileInfo.name(), projectInfo.owner(), projectInfo.website(), String.valueOf(fileInfo.fileId()));
                files.add(e);

                // ' 001/354 '
                String progress = String.format("%1$" + (String.valueOf(total).length() * 2 + 1) + "s", ++i + "/" + total).replace(' ', '0') + " ";
                FileBase.log(this.title(), progress + "\u001B[33m" + (e.projectName()) + "\u001B[0m found");
            }

            return files;
        } catch (IOException e) {
            if (step < 10) {
                return this.retrieveFiles(map, ++step);
            }

            throw new IllegalStateException("Failed to retrieve project information for '" + this.title() + "'", e);
        }
    }
}

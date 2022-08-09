package org.moddingx.modlistcreator.platform;

import com.google.gson.JsonObject;
import org.moddingx.modlistcreator.ModListCreator;
import org.moddingx.modlistcreator.platform.curse.CurseModpack;
import org.moddingx.modlistcreator.platform.modrinth.ModrinthModpack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public abstract class Modpack {

    protected final List<ProjectEntry> files = new ArrayList<>();

    public abstract int formatVersion();

    public abstract String title();

    public abstract Minecraft minecraft();

    public abstract String version();

    public abstract PackType packType();

    public abstract Modpack load(JsonObject json);

    public abstract List<ProjectEntry> files();

    public abstract String authorLink(String author);

    public String game() {
        return "minecraft";
    }

    public static Modpack fromJson(File file) {
        try {
            return Modpack.fromJson(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to load file", e);
        }
    }

    public static Modpack fromJson(Reader reader) {
        return Modpack.fromJson(ModListCreator.GSON.fromJson(reader, JsonObject.class));
    }

    public static Modpack fromJson(JsonObject json) {
        if (json.has("manifestVersion") && json.get("manifestVersion").getAsInt() == CurseModpack.FORMAT_VERSION) {
            return new CurseModpack().load(json);
        }

        if (json.has("formatVersion") && json.get("formatVersion").getAsInt() == ModrinthModpack.FORMAT_VERSION) {
            return new ModrinthModpack().load(json);
        }

        throw new IllegalStateException("Json does not match any known modpack platform.");
    }

    public record ProjectEntry(String projectName, String fileName, String author, URI website, String fileId) {
    }

    public record Minecraft(String version, ModLoader loaders) {
    }

    public record ModLoader(String type, String version) {
    }

    public enum PackType {
        CURSEFORGE,
        MODRINTH
    }
}

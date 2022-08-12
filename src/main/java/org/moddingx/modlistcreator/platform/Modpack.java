package org.moddingx.modlistcreator.platform;

import com.google.gson.JsonElement;
import org.moddingx.modlistcreator.modlist.ModListCreator;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public interface Modpack {
    
    String title();
    Minecraft minecraft();
    String version();
    List<File> files();
    
    static Modpack loadZip(Path path) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + path.toAbsolutePath().normalize().toUri()), Map.of())) {
            for (Type type : Type.values()) {
                Path manifest = fs.getPath("/").resolve(type.manifestPath).toAbsolutePath().normalize();
                if (Files.isRegularFile(manifest)) {
                    return load(manifest, type);
                }
            }
            throw new IOException("Failed to load modpack: Format unknown, no manifest file found in archive");
        }
    }
    
    static Modpack load(Path path) throws IOException {
        JsonElement json;
        try (Reader reader = Files.newBufferedReader(path)) {
            json = ModListCreator.GSON.fromJson(reader, JsonElement.class);
        }
        for (Type type : Type.values()) {
            Optional<? extends Modpack> pack = type.factory.apply(json);
            if (pack.isPresent()) {
                return pack.get();
            }
        }
        throw new IOException("Failed to load modpack: Format unknown, manifest file has no known format");
    }
    
    static Modpack load(Path path, Type type) throws IOException {
        JsonElement json;
        try (Reader reader = Files.newBufferedReader(path)) {
            json = ModListCreator.GSON.fromJson(reader, JsonElement.class);
        }
        Optional<? extends Modpack> pack = type.factory.apply(json);
        if (pack.isEmpty()) {
            throw new IOException("Invalid " + type.name().toLowerCase(Locale.ROOT) + " modpack: Invalid manifest");
        } else {
            return pack.get();
        }
    }

    interface File {
        String projectSlug();
        String projectName();
        String fileName();
        String author();
        URI projectWebsite();
        URI fileWebsite();
        URI authorWebsite();
    }
    
    record DefaultFile(String projectSlug, String projectName, String fileName, String author, URI projectWebsite, URI fileWebsite, URI authorWebsite) implements File {}

    record Minecraft(String version, String loader, String loaderVersion) {}

    enum Type {
        CURSEFORGE("manifest.json", CurseModpack::load),
        MODRINTH("modrinth.index.json", ModrinthModpack::load);
        
        private final String manifestPath;
        private final IOFunction<JsonElement, Optional<? extends Modpack>> factory;

        Type(String manifestPath, IOFunction<JsonElement, Optional<? extends Modpack>> factory) {
            this.manifestPath = manifestPath;
            this.factory = factory;
        }
    }
    
    @FunctionalInterface
    interface IOFunction<T, R> {
        R apply(T arg) throws IOException;
    }
}
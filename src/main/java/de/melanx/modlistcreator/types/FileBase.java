package de.melanx.modlistcreator.types;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import com.therandomlabs.curseapi.project.CurseMember;
import de.melanx.modlistcreator.util.MapUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FileBase {
    protected final StringBuilder builder;
    protected final CurseModpack pack;
    protected final boolean detailed;
    protected final boolean headless;
    protected final List<CurseFile> projects = new ArrayList<>();

    protected FileBase(CurseModpack pack, boolean detailed, boolean headless) {
        this.builder = new StringBuilder();
        this.pack = pack;
        this.detailed = detailed;
        this.headless = headless;
        try {
            Map<CurseFile, String> map = new HashMap<>();
            this.pack.files().forEach(file -> {
                try {
                    map.put(file, file.project().name());
                } catch (CurseException e) {
                    e.printStackTrace();
                }
            });
            MapUtil.sortByValue(map).forEach((file, name) -> {
                this.projects.add(file);
            });
        } catch (CurseException e) {
            e.printStackTrace();
        }
    }

    public abstract void generateFile();

    protected abstract String getFormattedProject(CurseFile file);

    protected abstract String getFormattedAuthor(CurseMember member);

    public abstract String getExtension();

    protected void generateFinalFile() {
        if (this.builder.toString().isEmpty()) {
            throw new IllegalStateException("Nothing to write to the file!");
        }
        try {
            File file = new File("modlist." + this.getExtension());
            FileWriter writer = new FileWriter(file);
            writer.write(this.getContent());
            writer.close();
            System.out.println("Successfully generated " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContent() {
        return this.builder.toString();
    }

    protected String getHeader() {
        return String.format("%s - %s", this.pack.name(), this.pack.version());
    }
}

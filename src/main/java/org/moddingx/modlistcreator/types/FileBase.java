package org.moddingx.modlistcreator.types;

import org.moddingx.modlistcreator.curse.CurseModpack;
import io.github.noeppi_noeppi.tools.cursewrapper.api.response.FileInfo;
import io.github.noeppi_noeppi.tools.cursewrapper.api.response.ProjectInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public abstract class FileBase {
    protected final StringBuilder builder;
    protected final CurseModpack pack;
    protected final boolean detailed;
    protected final boolean headless;

    protected FileBase(CurseModpack pack, boolean detailed, boolean headless) {
        this.builder = new StringBuilder();
        this.pack = pack;
        this.detailed = detailed;
        this.headless = headless;
    }

    public abstract void generateFile(String name, File output);

    protected abstract String getFormattedProject(ProjectInfo project, FileInfo file);

    protected abstract String getFormattedAuthor(String member);

    public abstract String getExtension();

    protected void generateFinalFile(String name, File output) {
        if (this.builder.toString().isEmpty()) {
            throw new IllegalStateException("Nothing to write to the file!");
        }
        try {
            if (!output.exists()) {
                if (output.mkdirs()) {
                    System.out.println("Created output directory: " + output);
                }
            }
            File file = new File(Paths.get(output.toString()) + File.separator + name + "." + this.getExtension());
            FileWriter writer = new FileWriter(file);
            writer.write(this.getContent());
            writer.close();
            System.out.println("Successfully generated " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContent() {
        return this.builder.toString();
    }

    protected String getHeader() {
        return String.format("%s - %s", this.pack.getName(), this.pack.getVersion());
    }

    protected void log(String text) {
        System.out.println("[\u001B[32m" + this.pack.getName() + "\u001B[0m] " + text);
    }

    public static void log(String pack, String text) {
        System.out.println("[\u001B[32m" + pack + "\u001B[0m] " + text);
    }
}

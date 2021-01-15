package de.melanx.modlistcreator;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import com.therandomlabs.curseapi.project.CurseMember;
import com.therandomlabs.curseapi.project.CurseProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ModListCreator {

    public static void main(String[] args) throws CurseException, IOException {
        CurseModpack pack = CurseModpack.fromJSON(Paths.get("manifest.json"));
        List<String> projects = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        ExportType type = ExportType.HTML;
        if (args.length > 0) {
            if (args.length > 1) {
                throw new IllegalArgumentException("Too many arguments.");
            }
            type = ExportType.getType(args[0].replace("--", ""));
        }
        ExportType finalType = type;
        if (finalType == ExportType.HTML) builder.append("<ul>");
        pack.files().forEach(file -> {
            try {
                projects.add(String.format("%s%s (by %s)%s\n", finalType == ExportType.HTML ? "<li>" : "- ", getFormattedProject(file.project(), finalType), getFormattedAuthor(file.project().author(), finalType), finalType == ExportType.HTML ? "</li>" : ""));
            } catch (CurseException e) {
                e.printStackTrace();
            }
        });
        Collections.sort(projects);
        projects.forEach(builder::append);
        if (finalType == ExportType.HTML) builder.append("</ul>");
        String content = builder.toString();
        String file = "modlist." + (finalType == ExportType.HTML ? "html" : "md");
        File html = new File(file);
        FileWriter writer = new FileWriter(html);
        writer.write(content);
        writer.close();
        System.out.println("Successfully generated " + file);
    }

    private static String getFormattedProject(CurseProject project, ExportType type) {
        if (type == ExportType.MARKDOWN) {
            return String.format("[%s](https://www.curseforge.com/minecraft/mc-mods/%s)", project.name(), project.slug());
        }
        return String.format("<a href=\"https://www.curseforge.com/minecraft/mc-mods/%s\">%s</a>", project.slug(), project.name());
    }

    private static String getFormattedAuthor(CurseMember member, ExportType type) {
        if (type == ExportType.MARKDOWN) {
            return String.format("[%s](https://www.curseforge.com/members/%s/projects)", member.name(), member.name().toLowerCase());
        }
        return String.format("<a href=\"https://www.curseforge.com/members/%s/projects\">%s</a>", member.name().toLowerCase(), member.name());
    }
}

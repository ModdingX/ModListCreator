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

public class ModListCreator {

    public static void main(String[] args) throws CurseException, IOException {
		CurseModpack pack = CurseModpack.fromJSON(Paths.get("manifest.json"));
		List<String> projects = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		builder.append("<ul>");
		pack.files().forEach(file -> {
			try {
				projects.add(String.format("<li>%s (by %s)\n</li>", getFormattedProject(file.project()), getFormattedAuthor(file.project().author())));
			} catch (CurseException e) {
				e.printStackTrace();
			}
		});
		Collections.sort(projects);
		projects.forEach(builder::append);
		builder.append("</ul>");
		String content = builder.toString();
		File html = new File("modlist.html");
		FileWriter writer = new FileWriter(html);
		writer.write(content);
		writer.close();
    }

    private static String getFormattedProject(CurseProject project) {
    	return String.format("<a href=\"https://www.curseforge.com/minecraft/mc-mods/%s\">%s</a>", project.slug(), project.name());
	}

	private static String getFormattedAuthor(CurseMember member) {
		return String.format("<a href=\"https://www.curseforge.com/members/%s/projects\">%s</a>", member.name().toLowerCase(), member.name());
	}
}

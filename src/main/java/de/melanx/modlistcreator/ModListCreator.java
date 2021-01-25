package de.melanx.modlistcreator;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import de.melanx.modlistcreator.types.FileBase;
import de.melanx.modlistcreator.types.files.HtmlFile;
import de.melanx.modlistcreator.types.files.MarkdownFile;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModListCreator {
    public static void main(String[] args) throws CurseException {
        CurseModpack pack = CurseModpack.fromJSON(Paths.get("manifest.json"));
        boolean alreadyGenerated = false;
        List<String> argList = Arrays.asList(args);
        boolean detailed = argList.contains("--detailed");
        boolean headless = argList.contains("--headless");
        for (String arg : args) {
            if (arg.equals("--html")) {
                new Thread(() -> {
                    FileBase html = new HtmlFile(pack, detailed, headless);
                    html.generateFile();
                }).start();
                alreadyGenerated = true;
            } else if (arg.equals("--md") || arg.equals("--markdown")) {
                new Thread(() -> {
                    FileBase markdown = new MarkdownFile(pack, detailed, headless);
                    markdown.generateFile();
                }).start();
                alreadyGenerated = true;
            } else if (!arg.equals("--detailed") && !arg.equals("--headless")) {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        if (!alreadyGenerated) {
            Set<FileBase> set = new HashSet<>();
            set.add(new HtmlFile(pack, detailed, headless));
            set.add(new MarkdownFile(pack, detailed, headless));
            set.forEach(file -> new Thread(file::generateFile).start());
        }
    }
}

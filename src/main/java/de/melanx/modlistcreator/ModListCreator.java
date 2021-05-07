package de.melanx.modlistcreator;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import de.melanx.modlistcreator.types.FileBase;
import de.melanx.modlistcreator.types.files.HtmlFile;
import de.melanx.modlistcreator.types.files.MarkdownFile;
import de.melanx.modlistcreator.util.NameFormat;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ModListCreator {
    private static OptionSet optionSet;

    public static void main(String[] args) throws CurseException, InterruptedException, IOException {
        OptionParser parser = new OptionParser();
        List<String> markdown = new ArrayList<>();
        Collections.addAll(markdown, "md", "markdown");

        parser.accepts("help", "Prints this overview");
        parser.accepts("detailed", "Shows exact version of each mod");
        parser.accepts("headless", "Generates the file without pack name/version");
        parser.accepts("html", "Exports HTML files");
        parser.acceptsAll(markdown, "Exports Markdown files");
        OptionSpec<String> nameFormat = parser.accepts("nameFormat", "Allowed values: DEFAULT, VERSION, NAME and NAME_VERSION").withRequiredArg().ofType(String.class).defaultsTo("default");
        OptionSpec<File> toolDir = parser.accepts("workingDir", "Defines the path where input and output should be").withRequiredArg().ofType(File.class).defaultsTo(new File(Paths.get("").toUri()));
        OptionSpec<File> packs = parser.accepts("input", "Defines the input directory for multiple manifests").withRequiredArg().ofType(File.class);
        OptionSpec<File> output = parser.accepts("output", "Defines the output directory for generated files").withRequiredArg().ofType(File.class);
        OptionSpec<File> manifest = parser.accepts("manifest", "Defines manifest file").withRequiredArg().ofType(File.class);
        OptionSpec<String> empty = parser.nonOptions();
        optionSet = parser.parse(args);

        if (!optionSet.hasOptions() || optionSet.has("help")) {
            printHelp(parser);
            System.exit(0);
        }

        if (optionSet.has(packs) && optionSet.has(manifest)) {
            printHelp(parser);
            throw new IllegalArgumentException("Can't set a single manifest and a path with multiple manifests.");
        }

        List<String> list = optionSet.valuesOf(empty);
        if (!list.isEmpty()) {
            System.out.println("Completely ignored arguments: " + list);
        }
        
        Set<Thread> joins = new HashSet<>();

        NameFormat format = NameFormat.get(getValue(optionSet, nameFormat));
        File workDir = getValue(optionSet, toolDir);
        File inDir = optionSet.has(packs) ? getValue(optionSet, packs) : new File(workDir, "input/");
        File outDir = optionSet.has(output) ? getValue(optionSet, output) : new File(workDir, "output/");
        if (optionSet.has(packs)) {
            if (inDir.isDirectory()) {
                for (File file : Objects.requireNonNull(inDir.listFiles())) {
                    CurseModpack pack = CurseModpack.fromJSON(file.toPath());
                    generateForPack(
                            joins,
                            pack,
                            getFileName(format, pack, file.getName().replace(".json", "")),
                            outDir
                    );
                }
            } else {
                throw new IllegalArgumentException("Path to packs is no directory: " + inDir.toString());
            }
        } else {
            Path path = optionSet.has(manifest) ? getValue(optionSet, manifest).toPath() : Paths.get("manifest.json");
            CurseModpack pack = CurseModpack.fromJSON(path);
            generateForPack(
                    joins,
                    pack,
                    getFileName(format, pack),
                    outDir
            );
        }
        for (Thread t : joins) {
            t.join();
        }
        System.exit(0);
    }

    private static void generateForPack(Set<Thread> joins, CurseModpack pack, String name, File output) {
        boolean alreadyGenerated = false;
        boolean detailed = optionSet.has("detailed");
        boolean headless = optionSet.has("headless");

        if (optionSet.has("html")) {
            Thread t = new Thread(() -> {
                FileBase html = new HtmlFile(pack, detailed, headless);
                html.generateFile(name, output);
            });
            joins.add(t);
            t.start();
            alreadyGenerated = true;
        }

        if (optionSet.has("md") || optionSet.has("markdown")) {
            Thread t = new Thread(() -> {
                FileBase markdown = new MarkdownFile(pack, detailed, headless);
                markdown.generateFile(name, output);
            });
            joins.add(t);
            t.start();
            alreadyGenerated = true;
        }

        if (!alreadyGenerated) {
            Set<FileBase> set = new HashSet<>();
            set.add(new HtmlFile(pack, detailed, headless));
            set.add(new MarkdownFile(pack, detailed, headless));
            set.forEach(file -> {
                Thread t = new Thread(() -> file.generateFile(name, output));
                joins.add(t);
                t.start();
            });
        }
    }

    private static String getFileName(NameFormat format, CurseModpack pack) {
        return getFileName(format, pack, "");
    }

    private static String getFileName(NameFormat format, CurseModpack pack, String prefix) {
        switch (format) {
            case NAME:
                return pack.name();
            case VERSION:
                return pack.version();
            case NAME_VERSION:
                return pack.name() + " - " + pack.version();
            default:
            case DEFAULT:
                return !prefix.isEmpty() ? prefix + "-modlist" : "modlist";
        }
    }

    private static <T> T getValue(OptionSet set, OptionSpec<T> option) {
        try {
            return set.valueOf(option);
        } catch (Throwable throwable) {
            if (option instanceof ArgumentAcceptingOptionSpec) {
                ArgumentAcceptingOptionSpec<T> spec = (ArgumentAcceptingOptionSpec<T>) option;
                List<T> list = spec.defaultValues();
                if (!list.isEmpty()) {
                    return list.get(0);
                }
            }

            throw throwable;
        }
    }

    private static void printHelp(OptionParser parser) throws IOException {
        StringWriter writer = new StringWriter();
        parser.printHelpOn(writer);
        System.out.println(writer.toString());
    }
}

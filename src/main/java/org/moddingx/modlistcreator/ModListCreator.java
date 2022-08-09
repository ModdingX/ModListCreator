package org.moddingx.modlistcreator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.moddingx.cursewrapper.api.CurseWrapper;
import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.types.FileBase;
import org.moddingx.modlistcreator.types.files.HtmlFile;
import org.moddingx.modlistcreator.types.files.MarkdownFile;
import org.moddingx.modlistcreator.util.NameFormat;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

public class ModListCreator {

    public static Gson GSON;
    private static OptionSet optionSet;
    private static final CurseWrapper wrapper = new CurseWrapper(URI.create("https://curse.melanx.de/"));
    public static final Predicate<File> filePredicate = file -> file.getName().equals("modrinth.index.json") || file.getName().equals("manifest.json");

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
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
                    File zip = getManifestFromZip(outDir, file);

                    if (zip != null) {
                        file = zip;
                    }

                    if (!filePredicate.test(file) && file != zip) {
                        continue;
                    }
                    Modpack pack = Modpack.fromJson(file);

                    if (pack == null) {
                        continue;
                    }

                    generateForPack(
                            joins,
                            pack,
                            getFileName(format, pack, file.getName().replace(".json", "")),
                            outDir
                    );
                }
            } else {
                throw new IllegalArgumentException("Path to packs is no directory: " + inDir);
            }
        } else {
            File file = optionSet.has(manifest) ? getValue(optionSet, manifest) : Paths.get("manifest.json").toFile();
            if (file == null) {
                file = Paths.get("modrinth.index.json").toFile();
            }

            File zip = getManifestFromZip(outDir, file);

            if (zip != null) {
                file = zip;
            }
            Modpack pack = Modpack.fromJson(file);

            if (pack != null) {
                generateForPack(
                        joins,
                        pack,
                        getFileName(format, pack),
                        outDir
                );
            }
        }
        for (Thread t : joins) {
            t.join();
        }
        System.exit(0);
    }

    public static CurseWrapper getWrapper() {
        return wrapper;
    }

    private static void generateForPack(Set<Thread> joins, Modpack pack, String name, File output) {
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

    private static String getFileName(NameFormat format, Modpack pack) {
        return getFileName(format, pack, "");
    }

    private static String getFileName(NameFormat format, Modpack pack, String prefix) {
        return switch (format) {
            case NAME -> pack.title();
            case VERSION -> pack.version();
            case NAME_VERSION -> pack.title() + " - " + pack.version();
            case DEFAULT -> !prefix.isEmpty() ? prefix + "-modlist" : "modlist";
        };
    }

    private static <T> T getValue(OptionSet set, OptionSpec<T> option) {
        try {
            return set.valueOf(option);
        } catch (Throwable throwable) {
            if (option instanceof ArgumentAcceptingOptionSpec<T> spec) {
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
        System.out.println(writer);
    }

    private static File getManifestFromZip(File output, File input) {
        // Getting manifest.json from zip
        try (FileSystem fs = FileSystems.newFileSystem(input.toPath(), (ClassLoader) null)) {
            Path zipManifest = fs.getPath("manifest.json");
            if (!Files.exists(zipManifest)) {
                zipManifest = fs.getPath("modrinth.index.json");
            }

            if (Files.exists(zipManifest)) {
                if (!output.exists()) {
                    if (output.mkdirs()) {
                        System.out.println("Created output directory: " + output);
                    }
                }
                File tempFile = output.toPath().resolve("manifest" + UUID.randomUUID() + ".json").toFile();
                Files.copy(zipManifest, tempFile.toPath());
                input = tempFile;
                input.deleteOnExit();

                return input;
            }
        } catch (Exception e) {
            //
        }

        return null;
    }
}

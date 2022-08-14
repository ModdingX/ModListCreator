package org.moddingx.modlistcreator.modlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.moddingx.modlistcreator.output.OutputTarget;
import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.util.CommonUtils;
import org.moddingx.modlistcreator.util.EnumConverters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.BiFunction;

public class ModListCreator {

    public static Gson GSON;
    
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }

    public static void run(String[] args) throws IOException {
        OptionParser options = new OptionParser();
        OptionSpec<Void> specNoHeader = options.accepts("no-header", "Generates the file without pack name and version");
        OptionSpec<Void> specDetailed = options.accepts("detailed", "Shows exact version of each mod");
        OptionSpec<OutputTarget.Type> specFormat = options.accepts("format", "The output format to use").withRequiredArg().withValuesConvertedBy(EnumConverters.enumArg(OutputTarget.Type.class)).withValuesSeparatedBy(",").defaultsTo(OutputTarget.Type.MARKDOWN);
        OptionSpec<Path> specOutput = options.accepts("output", "Defines the output path for generated files. If --pattern is set, describes a directory for output files, else a concrete file.").withRequiredArg().withValuesConvertedBy(new PathConverter());
        OptionSpec<String> specPattern = options.accepts("pattern", "Defines the output file name pattern. %n is replaced with pack name, %v with pack version.").withRequiredArg().ofType(String.class);
        OptionSpec<Path> specInput = options.nonOptions("Input files. Can be either modpack zips or json files.").withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE));

        OptionSet set;
        try {
            set = options.parse(args);
            if (!set.has(specOutput)) CommonUtils.missing(options, specOutput);
            if (!set.has(specPattern) && set.valuesOf(specFormat).size() != 1) CommonUtils.missing(options, specPattern, "Name pattern needed for multiple output formats");
            if (!set.has(specPattern) && set.valuesOf(specInput).size() != 1) CommonUtils.missing(options, specPattern, "Name pattern needed for multiple input files");
            if (set.valuesOf(specInput).isEmpty()) CommonUtils.missing(options, specInput, "No inputs");
        } catch (OptionException e) {
            System.err.println(e.getMessage() + "\n");
            options.printHelpOn(System.err);
            System.exit(1);
            throw new Error();
        }

        BiFunction<Modpack, OutputTarget.Type, Path> outputPaths = CommonUtils.outputPathFunc(set.valueOf(specOutput), set.has(specPattern) ? set.valueOf(specPattern) : null);
        List<OutputTarget.Type> outputTypes = set.valuesOf(specFormat).stream().distinct().toList();
        boolean includeHeader = !set.has(specNoHeader);
        boolean detailed = set.has(specDetailed);
        List<Path> inputs = set.valuesOf(specInput);

        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(Math.min(inputs.size(), Runtime.getRuntime().availableProcessors() - 1));
        List<Future<?>> joins = new ArrayList<>();
        for (Path path : inputs) {
            joins.add(executor.submit(() -> {
                try {
                    Modpack pack = CommonUtils.fromPath(path);
                    for (OutputTarget.Type type : outputTypes) {
                        Path outputPath = outputPaths.apply(pack, type);
                        if (!Files.exists(outputPath.getParent())) {
                            Files.createDirectories(outputPath.getParent());
                        }
                        Files.writeString(outputPath, ModListFormatter.format(pack, type, includeHeader, detailed), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed for path: " + path, e);
                }
            }));
        }
        for (Future<?> future : joins) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            }
        }
        System.exit(0);
    }
}

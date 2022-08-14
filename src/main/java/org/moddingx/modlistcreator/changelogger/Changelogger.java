package org.moddingx.modlistcreator.changelogger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.moddingx.modlistcreator.output.OutputTarget;
import org.moddingx.modlistcreator.util.CommonUtils;
import org.moddingx.modlistcreator.util.EnumConverters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Changelogger {

    public static void run(String[] args) throws IOException {
        OptionParser options = new OptionParser();
        OptionSpec<Path> specOld = options.acceptsAll(List.of("o", "old"), "Defines the old modpack zip or json file").withOptionalArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING)).defaultsTo(Paths.get("old.json"));
        OptionSpec<Path> specNew = options.acceptsAll(List.of("n", "new"), "Defines the new modpack zip or json file").withOptionalArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING)).defaultsTo(Paths.get("new.json"));
        OptionSpec<String> specOutput = options.accepts("output", "Defines the output file name without extension").withOptionalArg().ofType(String.class).defaultsTo("changelog");
        OptionSpec<OutputTarget.Type> specFormat = options.accepts("format", "The output format to use").withRequiredArg().withValuesConvertedBy(EnumConverters.enumArg(OutputTarget.Type.class)).withValuesSeparatedBy(",").defaultsTo(OutputTarget.Type.MARKDOWN);

        OptionSet set;
        try {
            set = options.parse(args);
            Path from = set.valueOf(specOld);
            Path to = set.valueOf(specNew);

            if (!Files.exists(from)) {
                throw new IllegalStateException("File does not exist: " + from);
            }

            if (!Files.exists(to)) {
                throw new IllegalStateException("File does not exist: " + to);
            }

            OutputTarget.Type outputType = set.valueOf(specFormat);
            Path output = Paths.get(set.valueOf(specOutput) + "." + outputType.extension);
            Files.writeString(output, ChangelogFormatter.format(CommonUtils.fromPath(from), CommonUtils.fromPath(to), outputType), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.exit(0);
        } catch (OptionException e) {
            System.err.println(e.getMessage() + "\n");
            options.printHelpOn(System.err);
            System.exit(1);
            throw new Error();
        }
    }
}
package org.moddingx.modlistcreator.changelogger;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.moddingx.modlistcreator.output.OutputTarget;
import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.util.EnumConverters;
import org.moddingx.modlistcreator.util.OptionUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Changelogger {

    public static void run(String[] args) throws IOException {
        OptionParser options = new OptionParser();
        OptionSpec<Path> specOld = options.acceptsAll(List.of("o", "old"), "Defines the old modpack zip or json file").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        OptionSpec<Path> specNew = options.acceptsAll(List.of("n", "new"), "Defines the new modpack zip or json file").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        OptionSpec<String> specOutput = options.accepts("output", "Defines the output file name").withOptionalArg().ofType(String.class);
        OptionSpec<OutputTarget.Type> specFormat = options.accepts("format", "The output format to use").withRequiredArg().withValuesConvertedBy(EnumConverters.enumArg(OutputTarget.Type.class)).defaultsTo(OutputTarget.Type.MARKDOWN);

        OptionSet set;
        try {
            set = options.parse(args);
            if (!set.has(specOld)) OptionUtil.missing(options, specOld);
            if (!set.has(specNew)) OptionUtil.missing(options, specNew);
            if (!set.has(specOutput)) OptionUtil.missing(options, specOutput);
        } catch (OptionException e) {
            System.err.println(e.getMessage() + "\n");
            options.printHelpOn(System.err);
            System.exit(1);
            throw new Error();
        }

        Modpack from = Modpack.fromPath(set.valueOf(specOld));
        Modpack to = Modpack.fromPath(set.valueOf(specNew));
        Path output = Paths.get(set.valueOf(specOutput));
        Files.writeString(output, ChangelogFormatter.format(from, to, set.valueOf(specFormat)), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.exit(0);
    }
}

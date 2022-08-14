package org.moddingx.modlistcreator.util;

import joptsimple.OptionParser;
import joptsimple.OptionSpec;
import org.moddingx.modlistcreator.output.OutputTarget;
import org.moddingx.modlistcreator.platform.Modpack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

public class OptionUtil {

    public static <T> T missing(OptionParser options, OptionSpec<?> spec) throws IOException {
        return missing(options, spec, "Missing required option");
    }

    public static <T> T missing(OptionParser options, OptionSpec<?> spec, String msg) throws IOException {
        System.err.println(msg + ": " + spec + "\n");
        options.printHelpOn(System.err);
        System.exit(1);
        throw new Error();
    }

    public static BiFunction<Modpack, OutputTarget.Type, Path> outputPathFunc(Path basePath, @Nullable String pattern) {
        Path normPath = basePath.toAbsolutePath().normalize();
        if (pattern == null) return (pack, type) -> normPath;
        return (pack, type) -> normPath.resolve(pattern
                .replace("%n", pack.title().replace(' ', '_'))
                .replace("%v", pack.version().replace(' ', '_'))
                .replace("%%", "%") + "." + type.extension
        );
    }
}

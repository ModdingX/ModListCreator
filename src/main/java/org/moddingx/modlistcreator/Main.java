package org.moddingx.modlistcreator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.moddingx.modlistcreator.changelogger.Changelogger;
import org.moddingx.modlistcreator.modlist.ModListCreator;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }
    
    public static void main(String[] args) throws IOException {
        String cmd = args.length == 0 ? "" : args[0];
        String[] newArgs = new String[Math.max(0, args.length - 1)];
        if (newArgs.length > 0) {
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        }
        switch (cmd.toLowerCase(Locale.ROOT)) {
            case "modlist" -> ModListCreator.run(newArgs);
            case "changelog" -> Changelogger.run(newArgs);
            default -> {
                System.err.println("ModListCreator - Choose sub-command\n");
                System.err.println("  modlist:   Create a modlist file from a CurseForge or Modrinth modpack.");
                System.err.println("  changelog:   Create a changelog file from a CurseForge or Modrinth modpack.");
            }
        }
    }
}

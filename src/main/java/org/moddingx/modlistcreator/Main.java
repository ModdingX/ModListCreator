package org.moddingx.modlistcreator;

import org.moddingx.modlistcreator.modlist.ModListCreator;

import java.io.IOException;
import java.util.Locale;

public class Main {
    
    public static void main(String[] args) throws IOException {
        String cmd = args.length == 0 ? "" : args[0];
        String[] newArgs = new String[Math.max(0, args.length - 1)];
        if (newArgs.length > 0) {
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        }
        switch (cmd.toLowerCase(Locale.ROOT)) {
            case "modlist" -> ModListCreator.run(newArgs);
            default -> {
                System.err.println("ModListCreator - Choose sub-command\n");
                System.err.println("  modlist:   Create a modlist file from a CurseForge or Modrinth modpack.");
            }
        }
    }
}
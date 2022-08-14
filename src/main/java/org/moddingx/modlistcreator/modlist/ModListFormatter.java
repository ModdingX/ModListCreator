package org.moddingx.modlistcreator.modlist;

import org.moddingx.modlistcreator.output.OutputTarget;
import org.moddingx.modlistcreator.platform.Modpack;

import java.util.Comparator;

public class ModListFormatter {
    
    public static String format(Modpack pack, OutputTarget.Type outputType, boolean includeHeader, boolean detailed) {
        OutputTarget target = outputType.create();
        if (includeHeader) {
            target.addHeader(pack.title() + " - " + pack.version());
        }
        
        target.beginList(false);
        for (Modpack.File file : pack.files().stream().sorted(Comparator.comparing(Modpack.File::projectSlug)).toList()) {
            String projectPart = detailed ? target.formatLink(file.fileName(), file.fileWebsite()) : target.formatLink(file.projectName(), file.projectWebsite());
            target.addListElement(projectPart + " (by " + target.formatLink(file.author(), file.authorWebsite()) + ")");
        }
        target.endList();
        
        return target.result();
    }
}

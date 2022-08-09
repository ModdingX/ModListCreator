package org.moddingx.modlistcreator.types.files;

import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.types.FileBase;

import java.io.File;

public class HtmlFile extends FileBase {

    public HtmlFile(Modpack pack, boolean detailed, boolean headless) {
        super(pack, detailed, headless);
    }

    @Override
    public void generateFile(String name, File output) {
        if (!this.headless) {
            this.builder.append("<h2>");
            this.builder.append(this.getHeader());
            this.builder.append("</h2>");
            this.builder.append("\n\n");
        }

        this.pack.files().forEach(entry -> {
            this.builder.append("<li>");
            this.builder.append(this.getFormattedProject(entry));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(entry.author()));
            this.builder.append(")</li>\n");
        });

        this.generateFinalFile(name, output);
    }

    @Override
    protected String getFormattedProject(Modpack.ProjectEntry project) {
        return String.format("<a href=\"%s%s\">%s</a>",
                project.website(),
                this.detailed ? "/files/" + project.fileId() : "",
                this.detailed ? project.fileName() : project.projectName());
    }

    @Override
    protected String getFormattedAuthor(String member) {
        return String.format("<a href=\"" + this.pack.authorLink(member) + "\">%s</a>", member.toLowerCase(), member);
    }

    @Override
    public String getExtension() {
        return "html";
    }
}

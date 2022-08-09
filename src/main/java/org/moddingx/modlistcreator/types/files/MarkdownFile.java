package org.moddingx.modlistcreator.types.files;

import org.moddingx.modlistcreator.platform.Modpack;
import org.moddingx.modlistcreator.types.FileBase;

import java.io.File;

public class MarkdownFile extends FileBase {

    public MarkdownFile(Modpack pack, boolean detailed, boolean headless) {
        super(pack, detailed, headless);
    }

    @Override
    public void generateFile(String name, File output) {
        if (!this.headless) {
            this.builder.append("## ");
            this.builder.append(this.getHeader());
            this.builder.append("\n");
        }

        this.pack.files().forEach(entry -> {
            this.builder.append("- ");
            this.builder.append(this.getFormattedProject(entry));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(entry.author()));
            this.builder.append(")\n");
        });

        this.generateFinalFile(name, output);
    }

    @Override
    protected String getFormattedProject(Modpack.ProjectEntry project) {
        return String.format("[%s](%s%s)",
                this.detailed ? project.fileName() : project.projectName(),
                project.website(),
                this.detailed ? "/files/" + project.fileId() : "");
    }

    @Override
    protected String getFormattedAuthor(String member) {
        return String.format("[%s](" + this.pack.authorLink(member) + ")", member, member.toLowerCase());
    }

    @Override
    public String getExtension() {
        return "md";
    }
}

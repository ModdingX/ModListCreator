package de.melanx.modlistcreator.types.files;

import de.melanx.modlistcreator.curse.CurseModpack;
import de.melanx.modlistcreator.types.FileBase;
import io.github.noeppi_noeppi.tools.cursewrapper.api.response.FileInfo;
import io.github.noeppi_noeppi.tools.cursewrapper.api.response.ProjectInfo;

import java.io.File;

public class MarkdownFile extends FileBase {

    public MarkdownFile(CurseModpack pack, boolean detailed, boolean headless) {
        super(pack, detailed, headless);
    }

    @Override
    public void generateFile(String name, File output) {
        this.log("\u001B[31mPutting \u001B[32meverything \u001B[34mtogether\u001B[35m.\u001B[36m.\u001B[33m.\u001B[0m");
        if (!this.headless) {
            this.builder.append("## ");
            this.builder.append(this.getHeader());
            this.builder.append("\n");
        }

        this.pack.getFiles().forEach(entry -> {
            this.builder.append("- ");
            this.builder.append(this.getFormattedProject(entry.getProject(), entry.getFile()));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(entry.getProject().owner()));
            this.builder.append(")\n");
        });

        this.generateFinalFile(name, output);
    }

    @Override
    protected String getFormattedProject(ProjectInfo project, FileInfo file) {
        return String.format("[%s](%s%s)",
                this.detailed ? file.name() : project.name(),
                project.website(),
                this.detailed ? "/files/" + file.fileId() : "");
    }

    @Override
    protected String getFormattedAuthor(String member) {
        return String.format("[%s](https://www.curseforge.com/members/%s/projects)", member, member.toLowerCase());
    }

    @Override
    public String getExtension() {
        return "md";
    }
}

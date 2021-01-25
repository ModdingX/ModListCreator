package de.melanx.modlistcreator.types.files;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import com.therandomlabs.curseapi.project.CurseMember;
import com.therandomlabs.curseapi.project.CurseProject;
import de.melanx.modlistcreator.types.FileBase;

import java.io.File;

public class MarkdownFile extends FileBase {
    public MarkdownFile(CurseModpack pack, boolean detailed, boolean headless) {
        super(pack, detailed, headless);
    }

    @Override
    public void generateFile(String name, File output) {
        if (!headless) {
            this.builder.append("##");
            this.builder.append(this.getHeader());
            this.builder.append("\n");
        }
        this.projects.forEach(file -> {
            CurseProject project;
            try {
                project = file.project();
            } catch (CurseException e) {
                throw new IllegalStateException("Following file caused an error: " + file.toString());
            }
            this.builder.append("- ");
            this.builder.append(this.getFormattedProject(file));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(project.author()));
            this.builder.append(")\n");
        });
        this.generateFinalFile(name, output);
    }

    @Override
    protected String getFormattedProject(CurseFile file) {
        try {
            return String.format("[%s](https://www.curseforge.com/minecraft/mc-mods/%s%s)",
                    this.detailed ? file.displayName() : file.project().name(),
                    file.project().slug(),
                    this.detailed ? "/" + file.id() : "");
        } catch (CurseException e) {
            throw new IllegalStateException("Following file caused an error: " + file.toString(), e);
        }
    }

    @Override
    protected String getFormattedAuthor(CurseMember member) {
        return String.format("[%s](https://www.curseforge.com/members/%s/projects)", member.name(), member.name().toLowerCase());
    }

    @Override
    public String getExtension() {
        return "md";
    }
}

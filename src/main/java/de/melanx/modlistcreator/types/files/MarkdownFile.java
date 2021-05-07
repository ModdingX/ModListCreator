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
        System.out.println("[" + this.pack.name() + "] Putting everything together...");
        if (!this.headless) {
            this.builder.append("## ");
            this.builder.append(this.getHeader());
            this.builder.append("\n");
        }

        this.projects.forEach((project, file) -> {
            this.builder.append("- ");
            this.builder.append(this.getFormattedProject(project, file));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(project.author()));
            this.builder.append(")\n");
        });

        this.generateFinalFile(name, output);
    }

    @Override
    protected String getFormattedProject(CurseProject project, CurseFile file) {
        try {
            return String.format("[%s](https://www.curseforge.com/minecraft/%s/%s%s)",
                    this.detailed ? file.displayName() : project.name(),
                    project.categorySection().asCategory().slug(),
                    project.slug(),
                    this.detailed ? "/files/" + file.id() : "");
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

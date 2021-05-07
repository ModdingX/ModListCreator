package de.melanx.modlistcreator.types.files;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import com.therandomlabs.curseapi.project.CurseMember;
import com.therandomlabs.curseapi.project.CurseProject;
import de.melanx.modlistcreator.types.FileBase;

import java.io.File;

public class HtmlFile extends FileBase {

    public HtmlFile(CurseModpack pack, boolean detailed, boolean headless) {
        super(pack, detailed, headless);
    }

    @Override
    public void generateFile(String name, File output) {
        System.out.println("[" + this.pack.name() + "] Putting everything together...");
        if (!this.headless) {
            this.builder.append("<h2>");
            this.builder.append(this.getHeader());
            this.builder.append("</h2>");
            this.builder.append("\n\n");
        }

        this.projects.forEach((project, file) -> {
            this.builder.append("<li>");
            this.builder.append(this.getFormattedProject(project, file));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(project.author()));
            this.builder.append(")</li>\n");
        });

        this.generateFinalFile(name, output);
    }

    @Override
    protected String getFormattedProject(CurseProject project, CurseFile file) {
        try {
            return String.format("<a href=\"https://www.curseforge.com/minecraft/%s/%s%s\">%s</a>",
                    project.categorySection().asCategory().slug(),
                    project.slug(),
                    this.detailed ? "/files/" + file.id() : "",
                    this.detailed ? file.displayName() : project.name());
        } catch (CurseException e) {
            throw new IllegalStateException("Following file caused an error: " + file.toString(), e);
        }
    }

    @Override
    protected String getFormattedAuthor(CurseMember member) {
        return String.format("<a href=\"https://www.curseforge.com/members/%s/projects\">%s</a>", member.name().toLowerCase(), member.name());
    }

    @Override
    public String getExtension() {
        return "html";
    }
}

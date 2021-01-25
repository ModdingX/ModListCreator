package de.melanx.modlistcreator.types.files;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import com.therandomlabs.curseapi.project.CurseMember;
import com.therandomlabs.curseapi.project.CurseProject;
import de.melanx.modlistcreator.types.FileBase;

public class HtmlFile extends FileBase {
    public HtmlFile(CurseModpack pack, boolean detailed, boolean headless) {
        super(pack, detailed, headless);
    }

    @Override
    public void generateFile() {
        if (!headless) {
            this.builder.append("<h2>");
            this.builder.append(this.getHeader());
            this.builder.append("</h2>");
            this.builder.append("\n\n");
        }
        this.projects.forEach(file -> {
            CurseProject project;
            try {
                project = file.project();
            } catch (CurseException e) {
                throw new IllegalStateException("Following file caused an error: " + file.toString(), e);
            }
            this.builder.append("<li>");
            this.builder.append(this.getFormattedProject(file));
            this.builder.append(" (by ");
            this.builder.append(this.getFormattedAuthor(project.author()));
            this.builder.append(")</li>\n");
        });
        this.generateFinalFile();
    }

    @Override
    protected String getFormattedProject(CurseFile file) {
        try {
            return String.format("<a href=\"https://www.curseforge.com/minecraft/mc-mods/%s%s\">%s</a>",
                    file.project().slug(),
                    this.detailed ? "/" + file.id() : "",
                    this.detailed ? file.displayName() : file.project().name());
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

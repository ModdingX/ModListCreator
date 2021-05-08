package de.melanx.modlistcreator.util;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.game.CurseCategory;
import com.therandomlabs.curseapi.project.CurseProject;

public class ProjectEntry {

    private final CurseFile file;
    private final CurseProject project;
    private final CurseCategory category;

    public ProjectEntry(CurseFile file) throws CurseException {
        this.file = file;
        this.project = file.project();
        this.category = this.project.categorySection().asCategory();
    }

    public CurseFile getFile() {
        return this.file;
    }

    public CurseProject getProject() {
        return this.project;
    }

    public CurseCategory getCategory() {
        return this.category;
    }

    @Override
    public String toString() {
        return this.project.name() + "(File: " + this.file.displayName() + ", Category: " + this.category.name() + ")";
    }
}

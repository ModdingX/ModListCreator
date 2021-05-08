package de.melanx.modlistcreator.util;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.project.CurseProject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProjectCache {

    private static final Map<CurseFile, ProjectEntry> files = Collections.synchronizedMap(new HashMap<>());
    private static final Map<CurseProject, ProjectEntry> projects = Collections.synchronizedMap(new HashMap<>());

    public static ProjectEntry getOrCreateProjectEntry(CurseFile file) throws CurseException {
        synchronized (files) {
            if (files.containsKey(file)) {
                return files.get(file);
            }

            ProjectEntry entry = new ProjectEntry(file);
            files.put(file, entry);
            projects.put(entry.getProject(), entry);
            return entry;
        }
    }

    public static ProjectEntry getOrCreateProjectEntry(CurseProject project) throws CurseException {
        synchronized (projects) {
            if (projects.containsKey(project)) {
                return projects.get(project);
            }

            ProjectEntry entry = new ProjectEntry(project.files().first());
            files.put(entry.getFile(), entry);
            projects.put(project, entry);
            return entry;
        }
    }
}

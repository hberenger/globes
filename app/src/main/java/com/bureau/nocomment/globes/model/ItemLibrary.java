package com.bureau.nocomment.globes.model;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemLibrary {
    // Json ppties
    private List<Project> projects;
    private List<Table> tables;

    // Computes ppties
    private Map<Integer, Project> sortedProjects;

    public List<Project> getProjects() {
        return projects;
    }

    public List<Table> getTables() {
        return tables;
    }

    public Project findProject(int testProjectId) {
        if (tables == null) {
            return null;
        }
        if (sortedProjects == null) {
            sortedProjects = new HashMap<>(tables.size());
            for(Project p : projects) {
                sortedProjects.put(p.getId(), p);
            }
            // Make sure we don't have any duplicate id
            Assert.assertEquals(projects.size(), sortedProjects.size());
            // Make sure we don't have projects without id
            Assert.assertEquals(null, sortedProjects.get(0));
        }
        return sortedProjects.get(testProjectId);
    }

    public void localeDidChange() {
        for(Project p : projects) {
            p.resetLocalizedInfo();
        }
    }
}

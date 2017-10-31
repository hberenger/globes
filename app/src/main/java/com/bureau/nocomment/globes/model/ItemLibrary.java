package com.bureau.nocomment.globes.model;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemLibrary {
    // Json ppties
    private List<Project> projects;
    private List<Table> tables;
    private List<Route> routes;

    // Computes ppties
    private Map<Integer, List<Project>> sortedProjects;

    public List<Project> getProjects() {
        return projects;
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Project findProject(int projectId) {
        if (tables == null) {
            return null;
        }
        if (sortedProjects == null) {
            sortedProjects = new HashMap<>(tables.size());
            for(Project p : projects) {
                List<Project> projectsSharingSameId = sortedProjects.get(p.getId());
                if (projectsSharingSameId == null) {
                    projectsSharingSameId = new ArrayList<>();
                    sortedProjects.put(p.getId(), projectsSharingSameId);
                }
                projectsSharingSameId.add(p);
            }

            // Make sure we don't have projects without id
            Assert.assertEquals(null, sortedProjects.get(0));
        }
        List<Project> matchingProjects = sortedProjects.get(projectId);
        return (matchingProjects.size() > 0) ? matchingProjects.get(0) : null;
    }

    public void localeDidChange() {
        for(Project p : projects) {
            p.resetLocalizedInfo();
        }
    }
}

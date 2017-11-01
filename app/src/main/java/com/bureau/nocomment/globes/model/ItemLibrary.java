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
    private Map<Integer, Table> sortedTables;

    public List<Project> getProjects() {
        return projects;
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Project> projectsHavingId(int projectId) {
        if (projects == null) {
            return null;
        }
        if (sortedProjects == null) {
            sortedProjects = new HashMap<>(projects.size());
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
        return matchingProjects;
    }

    public Project findProject(int projectId) {
        List<Project> projects = projectsHavingId(projectId);
        return (projects != null && projects.size() > 0) ? projects.get(0) : null;
    }

    public Table findTable(int tableId) {
        if (tables == null) {
            return null;
        }
        if (sortedTables == null) {
            sortedTables = new HashMap<>(tables.size());
            for(Table t : tables) {
                sortedTables.put(t.getId(), t);
            }
            // Make sure we don't have any duplicate id
            Assert.assertEquals(tables.size(), sortedTables.size());
            // Make sure we don't have tables without id
            Assert.assertEquals(null, sortedTables.get(0));
        }
        return sortedTables.get(tableId);
    }

    public void localeDidChange() {
        for(Project p : projects) {
            p.resetLocalizedInfo();
        }
    }
}

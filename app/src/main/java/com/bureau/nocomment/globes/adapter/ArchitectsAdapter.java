
package com.bureau.nocomment.globes.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.view.ArchitectCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArchitectsAdapter extends ArrayAdapter<Project> {

    private List<Project> mProjects;
    private List<Project> mFilteredProjects;

    public ArchitectsAdapter(Context context) {
        super(context, 0);
        mProjects = new ArrayList<>();
    }

    public void setProjects(List<Project> projects) {
        this.mProjects.addAll(projects);
        clear();
        addAll(mProjects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Project project = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.view_architects_cell_root, parent, false);
        }

        ((ArchitectCell) convertView).configure(project, getContext());
        return convertView;
    }

    public Comparator<Project> getDateComparator(final boolean inverted) {
        return new Comparator<Project>() {
            @Override
            public int compare(Project lhs, Project rhs) {
                return (!inverted) ?
                        lhs.getDate().compareTo(rhs.getDate()) :
                        rhs.getDate().compareTo(lhs.getDate());
            }
        };
    }

    public Comparator<Project> getNameComparator(final boolean inverted) {
        return new Comparator<Project>() {
            @Override
            public int compare(Project lhs, Project rhs) {
                return (!inverted) ?
                        lhs.getAuthor().compareTo(rhs.getAuthor()) :
                        rhs.getAuthor().compareTo(lhs.getAuthor());
            }
        };
    }

    public Comparator<Project> getSizeComparator(final boolean inverted) {
        return new Comparator<Project>() {
            @Override
            public int compare(Project lhs, Project rhs) {
                return (!inverted) ?
                        lhs.getDiameter().compareTo(rhs.getDiameter()) :
                        rhs.getDiameter().compareTo(lhs.getDiameter());
            }
        };
    }

    public Comparator<Project> getCountryComparator(final boolean inverted) {
        return new Comparator<Project>() {
            @Override
            public int compare(Project lhs, Project rhs) {
                return (!inverted) ?
                        lhs.getLocalizationDescription().compareTo(rhs.getLocalizationDescription()) :
                        rhs.getLocalizationDescription().compareTo(lhs.getLocalizationDescription());
            }
        };
    }

    public Comparator<Project> getNumberComparator(final boolean inverted) {
        return new Comparator<Project>() {
            @Override
            public int compare(Project lhs, Project rhs) {
                return (!inverted) ?
                        lhs.getId() - rhs.getId() :
                        rhs.getId() - lhs.getId();
            }
        };
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence pattern) {
                FilterResults results = new FilterResults();

                if (pattern == null || pattern.length() == 0) {
                    results.count = mProjects.size();
                    results.values = mProjects;
                } else {
                    List<Project> filteredProjects = new ArrayList<>();
                    for (Project p : mProjects) {
                        if (p.matchesCompositePattern(pattern.toString())) {
                            filteredProjects.add(p);
                        }
                    }
                    results.count = filteredProjects.size();
                    results.values = filteredProjects;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredProjects = (ArrayList<Project>) results.values;
                clear();
                addAll(mFilteredProjects);
            }
        };
    }
}


package com.bureau.nocomment.globes.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.view.ArchitectCell;

import java.util.ArrayList;
import java.util.List;

public class ArchitectsAdapter extends ArrayAdapter<Project> {

    private List<Project> mProjects;

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
}

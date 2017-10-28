
package com.bureau.nocomment.globes.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.model.Route;
import com.bureau.nocomment.globes.view.ArchitectCell;

import java.util.ArrayList;
import java.util.List;

public class RoutesAdapter extends ArrayAdapter<Route> {

    private List<Route> mRoutes;

    public RoutesAdapter(Context context) {
        super(context, 0);
        mRoutes = new ArrayList<>();
    }

    public void setRoutes(List<Route> routes) {
        this.mRoutes.addAll(routes);
        clear();
        addAll(mRoutes);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Route route = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.view_architects_cell_root, parent, false);
        }

        Project project = new Project();

        ((ArchitectCell) convertView).configure(project, getContext());
        return convertView;
    }
}

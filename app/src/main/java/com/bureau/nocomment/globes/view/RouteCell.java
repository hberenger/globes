package com.bureau.nocomment.globes.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Route;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RouteCell extends ConstraintLayout {

    @Bind(R.id.route_name)          TextView routeNameView;
    @Bind(R.id.route_description)   TextView routeDescriptionView;
    @Bind(R.id.route_length)        TextView routeLengthView;

    public RouteCell(Context context) {
        super(context);
        init(context);
    }

    public RouteCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_routes_cell_contents, this);
        ButterKnife.bind(this);
    }

    public void configure(Route route, Context context) {
        routeNameView.setText(route.getTitle());
        routeDescriptionView.setText(route.getDescription());

        int length = route.getLength();
        Resources res = getResources();
        String lengthString = res.getQuantityString(R.plurals.routeLength, length, length);
        routeLengthView.setText(lengthString);
    }

}

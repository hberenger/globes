package com.bureau.nocomment.globes.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Project;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ArchitectCell extends ConstraintLayout {

    @Bind(R.id.picto) ImageView pictogram;

    @Bind(R.id.architect_name)    TextView architectNameTextView;
    @Bind(R.id.project_name)    TextView projectNameTextView;

    @Bind(R.id.date) TextView dateTextView;
    @Bind(R.id.place) TextView placeTextView;
    @Bind(R.id.size) TextView sizeTextView;

    public ArchitectCell(Context context) {
        super(context);
        init(context);
    }

    public ArchitectCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ArchitectCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_architects_cell_contents, this);
        ButterKnife.bind(this);
    }

    public void configure(Project project, Context context) {
        architectNameTextView.setText(project.getAuthor());
        projectNameTextView.setText(project.getName());
        dateTextView.setText(project.getDateDescription());
        placeTextView.setText(project.getLocalizationDescription());
        String diameter = String.format(Locale.getDefault(), "%.0fm", project.getDiameter());
        sizeTextView.setText(diameter);
    }

}

package com.bureau.nocomment.globes.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Project;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ArchitectCell extends ConstraintLayout {

     @Bind(R.id.architect_name)    TextView nameTextView;
     @Bind(R.id.place) TextView placeTextView;

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

    public void configure(Project project) {
        // TODO
        // nameTextView.setText(project.getName());...
    }

}

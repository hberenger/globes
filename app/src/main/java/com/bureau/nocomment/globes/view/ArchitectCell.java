package com.bureau.nocomment.globes.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.Project;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ArchitectCell extends RelativeLayout {

     @Bind(R.id.name)    TextView nameTextView;
     @Bind(R.id.country) TextView countryTextView;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArchitectCell(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

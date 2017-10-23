package com.bureau.nocomment.globes.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.adapter.ArchitectsAdapter;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArchitectsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    static final String LOGTAG = "ArchitectsFragment";

    private Boolean mInverseSort = false;
    private int     mLastSortField = 0;

    public interface ProjectSelectedObserver {
        void onProjectSelected(Project p);
    }

    @Bind(R.id.architects_list) ListView          mArchitectsList;
    private                     ArchitectsAdapter mArchitectsAdapter;

    @Bind(R.id.sort_by_name)    Button     mSortByName;
    @Bind(R.id.sort_by_date)    Button     mSortByDate;
    @Bind(R.id.sort_by_size)    Button     mSortBySize;
    @Bind(R.id.sort_by_index)   Button     mSortByNumber;
    @Bind(R.id.sort_by_country) Button     mSortByCountry;

    @Bind(R.id.search_text)     EditText   mSearchString;

    private ProjectSelectedObserver mProjectSelectedObserver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_architects, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.architects_list);

        View header = LayoutInflater.from(getContext()).inflate(R.layout.view_architects_header, listView, false);
        listView.addHeaderView(header, "BandeauTitle", false);
        View footer = LayoutInflater.from(getContext()).inflate(R.layout.view_architects_footer, listView, false);
        listView.addFooterView(footer);

        mArchitectsAdapter = new ArchitectsAdapter(getContext());
        listView.setAdapter(mArchitectsAdapter);

        listView.setOnItemClickListener(this);

        ButterKnife.bind(this, rootView);

        mSearchString.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mArchitectsAdapter.getFilter().filter(charSequence.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mProjectSelectedObserver = (ProjectSelectedObserver) context;
        } catch (ClassCastException e) {
            Log.d(LOGTAG, "Could not attach context as ProjectSelected observer");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        populate();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSearchHeader();
    }

    // Private

    private void populate() {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();

        mArchitectsAdapter.setProjects(projects);
    }

    @OnClick(R.id.gotop)
    void onGoTop() {
        // TODO : go top
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();
        Project p = projects.get(i - 1); // minus one because of header
        if (mProjectSelectedObserver != null) {
            mProjectSelectedObserver.onProjectSelected(p);
        }
    }

    @OnClick(R.id.sort_by_name)
    void onSortByName() {
        updateSortDirection(R.id.sort_by_name);
        mArchitectsAdapter.sort(mArchitectsAdapter.getNameComparator(mInverseSort));
    }

    @OnClick(R.id.sort_by_size)
    void onSortBySize() {
        updateSortDirection(R.id.sort_by_size);
        mArchitectsAdapter.sort(mArchitectsAdapter.getSizeComparator(mInverseSort));
    }

    @OnClick(R.id.sort_by_country)
    void onSortByCountry() {
        updateSortDirection(R.id.sort_by_country);
        mArchitectsAdapter.sort(mArchitectsAdapter.getCountryComparator(mInverseSort));
    }

    @OnClick(R.id.sort_by_index)
    void onSortByNumber() {
        updateSortDirection(R.id.sort_by_index);
        mArchitectsAdapter.sort(mArchitectsAdapter.getNumberComparator(mInverseSort));
    }

    @OnClick(R.id.sort_by_date)
    void onSortByDate() {
        updateSortDirection(R.id.sort_by_date);
        mArchitectsAdapter.sort(mArchitectsAdapter.getDateComparator(mInverseSort));
    }

    private void updateSortDirection(int tappedFieldId) {
        if (mLastSortField == tappedFieldId) {
            mInverseSort = !mInverseSort;
        } else {
            mInverseSort = false;
        }
        mLastSortField = tappedFieldId;
    }

    @OnClick(R.id.search_close)
    void onDismissSearch() {
        hideSoftKeyboard();
        mSearchString.setText("");
        // Hacky but difficult to scroll correctly without it
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSearchHeader();
            }
        }, 150);
    }

    private void hideSearchHeader() {
        if (mSearchString.getText().toString().isEmpty()) {
            mArchitectsList.smoothScrollToPositionFromTop(1, 0);
        }
    }

    private void hideSoftKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

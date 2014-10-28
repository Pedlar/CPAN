package org.notlocalhost.cpan.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;

import java.util.concurrent.Future;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnEditorAction;

public class SearchFragment extends BaseFragment {
    public static final String TAG = "SearchFragment";

    @InjectView(R.id.search)
    EditText mSearchEditText;

    @Inject
    SearchInteractor mSearchInteractor;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @OnEditorAction(R.id.search)
    public boolean onEditorAction(TextView v, int actionId) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEARCH:
                if(v.getText().length() > 0) {
                    Future future = mSearchInteractor.performSearch(v.getText().toString(), mListener);
                } else {
                    return true; // Keep the keyboard open.
                }
                break;
        }
        return false;
    }
}

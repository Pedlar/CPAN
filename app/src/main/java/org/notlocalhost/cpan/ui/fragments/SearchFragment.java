package org.notlocalhost.cpan.ui.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.notlocalhost.cpan.Constants;
import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;
import org.notlocalhost.cpan.view.NavigationDrawable;

import java.util.concurrent.Future;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchFragment extends BaseFragment {
    public static final String TAG = "SearchFragment";

    @InjectView(R.id.search)
    SearchView mSearchView;

    @InjectView(R.id.window_tint)
    View mWindowTint;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @Inject
    SearchInteractor mSearchInteractor;

    private float mOriginalY;
    private float mOriginalScaleX;

    private Handler mHandler = new Handler();

    ValueAnimator mColorAnimation;
    ValueAnimator mMarginAnimation;

    private static final int ANIMATION_TIME = 400;

    private CursorAdapter mAdapter;

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
        ((LayerDrawable)mSearchView.getBackground()).getDrawable(1).setAlpha(0);

        mListener.getToolbar().setVisibility(View.GONE);

        NavigationDrawable navigationDrawable = new NavigationDrawable(getActivity(), Color.RED, NavigationDrawable.Stroke.REGULAR);

        mToolbar.setNavigationIcon(navigationDrawable);
        mToolbar.setTitle(mListener.getToolbar().getTitle());

        setupSearchView();

        setupAnimations();

        return view;
    }

    private void setupAnimations() {
        int colorFrom = 0x00ffffff;
        int colorTo = 0xffffffff;
        final int colorToAlpha = Color.alpha(colorTo);
        mColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        mColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                LayerDrawable lDrawable = (LayerDrawable)mSearchView.getBackground();
                Integer animatedAlpha = Color.alpha((Integer) animator.getAnimatedValue());
                lDrawable.getDrawable(1).setAlpha(animatedAlpha);

                lDrawable.getDrawable(0).setAlpha(colorToAlpha - animatedAlpha);
            }
        });
        mColorAnimation.setDuration(ANIMATION_TIME);

        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)mSearchView.getLayoutParams();
        mMarginAnimation = ValueAnimator.ofInt(lParams.leftMargin);
        mMarginAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int margin = (Integer)animation.getAnimatedValue();
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)mSearchView.getLayoutParams();
                lParams.leftMargin = margin;
                lParams.rightMargin = margin;
                mSearchView.setLayoutParams(lParams);
            }
        });
        mMarginAnimation.setDuration(ANIMATION_TIME);
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() > 0) {
                    Future future = mSearchInteractor.performSearch(s, mListener, null);
                } else {
                    return true; // Keep the keyboard open.
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Cursor c = mSearchInteractor.getSearchSuggestions(s);
                mAdapter.changeCursor(c);
                return false;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mOriginalY = v.getY();
                    mOriginalScaleX = v.getScaleX();
                    v.animate()
                            .y(1)
                            .setDuration(ANIMATION_TIME)
                            .start();
                    mWindowTint.animate().alpha(.5f).setDuration(ANIMATION_TIME).start();
                    mColorAnimation.start();
                    mMarginAnimation.reverse();
                } else if (mOriginalY != 0) {
                    v.animate().y(mOriginalY).scaleX(mOriginalScaleX).setDuration(ANIMATION_TIME).start();
                    mWindowTint.animate().alpha(0).setDuration(ANIMATION_TIME).start();
                    mColorAnimation.reverse();
                    mMarginAnimation.start();
                }
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor c = (Cursor)mAdapter.getItem(i);
                String query = c.getString(c.getColumnIndex(Constants.SEARCH_STRING));
                mSearchView.setQuery(query, true);
                return true;
            }
        });

        mAdapter = mSearchInteractor.getCursorAdapter(getActivity());
        mSearchView.setSuggestionsAdapter(mAdapter);

        /* Hack needed to unfocus on launch so animations will work */
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchView.clearFocus();
            }
        }, 100);
    }
}

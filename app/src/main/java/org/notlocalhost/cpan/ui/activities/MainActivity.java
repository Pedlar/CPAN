package org.notlocalhost.cpan.ui.activities;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.notlocalhost.cpan.Constants;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.ui.fragments.BaseFragment;
import org.notlocalhost.cpan.ui.fragments.SearchFragment;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;
import org.notlocalhost.cpan.view.NavigationDrawable;

import java.util.List;
import java.util.concurrent.Future;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity
        implements FragmentInterface {

    ProgressDialog progressDialog;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.top_fab)
    ImageButton mTopFab;

    @InjectView(R.id.fab_search)
    SearchView mFabSearch;

    @Inject
    SearchInteractor mSearchInteractor;

    private NavigationDrawable navigationDrawable;

    CursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        navigationDrawable = new NavigationDrawable(this, Color.RED, NavigationDrawable.Stroke.REGULAR);

        mToolbar.setTitle(R.string.app_name);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(navigationDrawable.getIconState() == NavigationDrawable.IconState.ARROW) {
                    //navigationDrawable.animateIconState(NavigationDrawable.IconState.BURGER, true);
                    onBackPressed();
                }
            }
        });
        mToolbar.setNavigationIcon(navigationDrawable);

        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, SearchFragment.newInstance(), SearchFragment.TAG)
                    .commit();
        }

        setFabShadow(mTopFab);

        mTopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFabSearch.getVisibility() == View.VISIBLE)
                    mFabSearch.setVisibility(View.INVISIBLE);
                else
                    mFabSearch.setVisibility(View.VISIBLE);
            }
        });

        mAdapter = mSearchInteractor.getCursorAdapter(this);
        mFabSearch.setSuggestionsAdapter(mAdapter);
        mFabSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() > 0) {
                    mFabSearch.setVisibility(View.INVISIBLE);
                    Future future = mSearchInteractor.performSearch(s, MainActivity.this, null);
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

        mFabSearch.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor c = (Cursor) mAdapter.getItem(i);
                String query = c.getString(c.getColumnIndex(Constants.SEARCH_STRING));
                mFabSearch.setQuery(query, true);
                return true;
            }
        });
    }

    private void setFabShadow(ImageButton fab) {
        Drawable background = fab.getBackground();
        if (background instanceof LayerDrawable) {
            LayerDrawable layers = (LayerDrawable) background;
            if (layers.getNumberOfLayers() == 2) {
                Drawable shadow = layers.getDrawable(0);
                Drawable circle = layers.getDrawable(1);
                if (shadow instanceof GradientDrawable) {
                    ((GradientDrawable) shadow.mutate()).setGradientRadius(getShadowRadius(shadow, circle));
                }
            }
        }
        fab.setBackgroundDrawable(background);
    }

    private int getShadowRadius(Drawable shadow, Drawable circle) {
        int radius = 0;
        if (shadow != null && circle != null) {
            Rect rect = new Rect();
            radius = (circle.getIntrinsicWidth() + (shadow.getPadding(rect) ? rect.left + rect.right : 0)) / 2;
        }
        return Math.max(1, radius);
    }

    private BaseFragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for(Fragment fragment : fragments){
            if(fragment != null && fragment.isVisible() && fragment instanceof BaseFragment)
                return (BaseFragment)fragment;
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mFabSearch.getVisibility() == View.VISIBLE) {
            mFabSearch.setVisibility(View.INVISIBLE);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                BaseFragment fragment = getVisibleFragment();
                if(fragment != null) {
                    if(fragment.onBackPressed()) {
                        return;
                    }
                }

                int size = fragmentManager.getBackStackEntryCount();
                if (size > 0) {
                    fragmentManager.popBackStack();
                    return;
                }
            }
            super.onBackPressed();
        }
    }

    @Override
    public void showProgress() {
        if(progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, null);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setContentView(new ProgressBar(this));
            progressDialog.setCancelable(false);
        } else {
            progressDialog.show();
        }
    }

    @Override
    public void dismissProgress() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void openFragment(Fragment fragment, String tag) {
        openFragment(fragment, tag, null, null);
    }

    @Override
    public void openFragment(Fragment fragment, String tag, View sharedView, String sharedName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, tag)
                .addToBackStack(tag);

        if(sharedView != null && sharedName != null) {
            fragmentTransaction.addSharedElement(sharedView, sharedName);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }

        fragmentTransaction.commit();
    }

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public NavigationDrawable getNavigationDrawable() {
        return navigationDrawable;
    }

    @Override
    public void toggleTopFab(boolean show) {
        mTopFab.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

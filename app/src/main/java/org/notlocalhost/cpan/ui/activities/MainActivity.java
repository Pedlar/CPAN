package org.notlocalhost.cpan.ui.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.ui.fragments.SearchFragment;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;
import org.notlocalhost.cpan.view.NavigationDrawable;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity
        implements FragmentInterface {

    ProgressDialog progressDialog;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private NavigationDrawable navigationDrawable;

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
                    navigationDrawable.animateIconState(NavigationDrawable.IconState.BURGER, true);
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager != null) {
            int size = fragmentManager.getBackStackEntryCount();
            if(size > 0) {
                fragmentManager.popBackStack();
                return;
            }
        }
        super.onBackPressed();
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
}

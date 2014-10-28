package org.notlocalhost.cpan.ui.activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.ui.fragments.SearchFragment;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;

public class MainActivity extends BaseActivity
        implements FragmentInterface {

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}

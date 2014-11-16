package org.notlocalhost.cpan.ui.interfaces;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.notlocalhost.cpan.view.NavigationDrawable;

/**
 * Created by pedlar on 10/11/14.
 */
public interface FragmentInterface {
    public void showProgress();
    public void dismissProgress();
    public void openFragment(Fragment fragment, String tag);
    public void openFragment(Fragment fragment, String tag, View sharedView, String sharedName);
    public Toolbar getToolbar();
    public NavigationDrawable getNavigationDrawable();
    public void toggleTopFab(boolean show);
}

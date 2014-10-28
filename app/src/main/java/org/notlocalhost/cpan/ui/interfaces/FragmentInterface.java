package org.notlocalhost.cpan.ui.interfaces;

import android.support.v4.app.Fragment;

/**
 * Created by pedlar on 10/11/14.
 */
public interface FragmentInterface {
    public void showProgress();
    public void dismissProgress();
    public void openFragment(Fragment fragment, String tag);
}

package org.notlocalhost.cpan.ui.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;

import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;

/**
 * Created by pedlar on 10/12/14.
 */
public class BaseFragment extends Fragment {
    protected FragmentInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}

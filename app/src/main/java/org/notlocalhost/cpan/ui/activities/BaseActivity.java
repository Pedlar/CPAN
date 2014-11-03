package org.notlocalhost.cpan.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.notlocalhost.cpan.Injector;

/**
 * Created by pedlar on 10/11/14.
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Injector.inject(this);
    }
}

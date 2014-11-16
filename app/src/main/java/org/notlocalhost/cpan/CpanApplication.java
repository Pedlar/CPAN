package org.notlocalhost.cpan;

import android.app.Application;

/**
 * Created by pedlar on 10/11/14.
 */
public class CpanApplication extends Application {

    @Override
    public void onCreate() {
        Injector.init(Modules.list(this));
    }

}

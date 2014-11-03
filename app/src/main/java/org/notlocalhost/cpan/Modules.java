package org.notlocalhost.cpan;

import android.content.Context;

import org.notlocalhost.cpan.data.DataModule;
import org.notlocalhost.cpan.ui.UiModule;

/**
 * Created by pedlar on 10/11/14.
 */
public class Modules {
    public static Object[] list(Context application) {
        return new Object[] {
            new DataModule(application),
            new UiModule()
        };
    }

    private Modules() {
        // no-op
    }
}

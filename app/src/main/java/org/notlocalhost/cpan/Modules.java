package org.notlocalhost.cpan;

import org.notlocalhost.cpan.data.DataModule;
import org.notlocalhost.cpan.ui.UiModule;

/**
 * Created by pedlar on 10/11/14.
 */
public class Modules {
    public static Object[] list() {
        return new Object[] {
            new DataModule(),
            new UiModule()
        };
    }

    private Modules() {
        // no-op
    }
}

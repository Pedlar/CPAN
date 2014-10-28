package org.notlocalhost.cpan;

import dagger.ObjectGraph;

/**
 * Created by pedlar on 10/11/14.
 */
public class Injector {
    private static ObjectGraph mObjectGraph;

    private static void initInternal(Object... modules) {
        mObjectGraph = ObjectGraph.create(modules);
    }

    public static ObjectGraph init(Object... modules) {
        if(mObjectGraph == null) {
            initInternal(modules);
        } else {
            return mObjectGraph.plus(modules);
        }
        return mObjectGraph;
    }

    public static void init(Object target, Object... modules) {
        init(modules).inject(target);
    }

    public static void inject(Object target) {
        mObjectGraph.inject(target);
    }

    private Injector() {
        //no-op
    }
}

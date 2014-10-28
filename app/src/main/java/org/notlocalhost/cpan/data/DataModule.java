package org.notlocalhost.cpan.data;

import android.support.annotation.NonNull;

import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.ui.activities.BaseActivity;
import org.notlocalhost.cpan.ui.activities.MainActivity;
import org.notlocalhost.cpan.ui.fragments.SearchFragment;
import org.notlocalhost.metacpan.MetaCpan;
import org.notlocalhost.metacpan.services.MetaApiService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by pedlar on 10/11/14.
 */
@Module(
        injects = {
                MainActivity.class, SearchFragment.class
        },
        complete = false,
        library = true
)
public class DataModule {
    public DataModule() {

    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(10);
    }

    @Provides
    @Singleton
    public ApiInteractor provideApiInteractor() {
        return new ApiInteractorImpl();
    }

}

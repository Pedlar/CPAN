package org.notlocalhost.cpan.ui;

import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.ui.fragments.ModuleDetailsFragment;
import org.notlocalhost.cpan.ui.fragments.SearchFragment;
import org.notlocalhost.cpan.ui.fragments.SearchResultFragment;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;

import java.util.concurrent.ExecutorService;

import dagger.Module;
import dagger.Provides;

/**
 * Created by pedlar on 10/11/14.
 */
@Module(
        injects = {
                SearchFragment.class, SearchResultFragment.class, ModuleDetailsFragment.class
        },
        library = true,
        complete = false
)
public class UiModule {

    @Provides
    public SearchInteractor provideSearchInteractor(ExecutorService executorService, ApiInteractor apiInteractor) {
        return new SearchInteractorImpl(executorService, apiInteractor);
    }
}

package org.notlocalhost.cpan.ui;

import android.util.Log;

import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.fragments.SearchResultFragment;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;
import org.notlocalhost.metacpan.models.Release;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 * This is the Interactor Class that handles the Logic behind searching, and where to go with the results
 *
 * Created by pedlar on 10/11/14.
 */
public class SearchInteractorImpl implements SearchInteractor {
    ApiInteractor mApiInteractor;
    ExecutorService mThreadExecutor;

    public SearchInteractorImpl(ExecutorService executorService, ApiInteractor apiInteractor) {
        mThreadExecutor = executorService;
        mApiInteractor = apiInteractor;
    }

    public Future<List<Release>> getMoreResults(final String query, int size, int offset) {
        return internalPerformSearch(query, null, 10, offset);
    }

    public Future<List<Release>> performSearch(final String query, final FragmentInterface listener) {
        if(listener != null) listener.showProgress();

        return internalPerformSearch(query, listener, 10, 0);
    }

    public Observable<SearchModel> getReleaseInformation(final List<Release> releeaseList) {
        return Observable.create(new Observable.OnSubscribe<SearchModel>() {
            @Override
            public void call(Subscriber<? super SearchModel> subscriber) {
                for(Release release : releeaseList) {
                    try {
                        SearchModel searchModel = new SearchModel();
                        searchModel.release = release;
                        searchModel.author = mApiInteractor.getAuthor(release.getAuthor());
                        searchModel.rating = searchModel.release.getRating();
                        subscriber.onNext(searchModel);
                    } catch(RetrofitError error) {
                        Log.w("SearchInteractorImpl", "Error fetching module information", error);
                    }
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    private Future<List<Release>> internalPerformSearch(String query, final FragmentInterface listener, final int size, final int offset) {
        final String finalQuery = query.replace("::", "-");
        return mThreadExecutor.submit(new Callable<List<Release>>() {
            @Override
            public List<Release> call() throws Exception {
                List<Release> releaseSearch = mApiInteractor.searchRelease(finalQuery + " AND status:latest", size, offset);

                if(listener != null) {
                    SearchResultFragment fragment = SearchResultFragment.newInstance(finalQuery, new ArrayList<Release>(releaseSearch));
                    listener.dismissProgress();
                    listener.openFragment(fragment, SearchResultFragment.TAG);
                }
                return releaseSearch;
            }
        });
    }
}

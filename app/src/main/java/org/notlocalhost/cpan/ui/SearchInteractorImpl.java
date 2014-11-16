package org.notlocalhost.cpan.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import org.notlocalhost.cpan.Constants;
import org.notlocalhost.cpan.data.SearchHistory;
import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.data.interfaces.DataInteractor;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.fragments.SearchResultFragment;
import org.notlocalhost.cpan.ui.interfaces.Callback;
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
import rx.functions.Func1;
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
    DataInteractor mDataInteractor;

    public SearchInteractorImpl(ExecutorService executorService, ApiInteractor apiInteractor, DataInteractor dataInteractor) {
        mThreadExecutor = executorService;
        mApiInteractor = apiInteractor;
        mDataInteractor = dataInteractor;
    }

    public Future<List<Release>> getMoreResults(final String query, int size, int offset) {
        return internalPerformSearch(query, null, null, 10, offset);
    }

    public Future<List<Release>> performSearch(final String query, final FragmentInterface listener, final Callback<ArrayList<Release>> callback) {
        if(listener != null) listener.showProgress();
        mDataInteractor.addSearchHistory(query);
        return internalPerformSearch(query, listener, callback, 10, 0);
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

    @Override
    public Cursor getSearchSuggestions(String search) {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, Constants.SEARCH_STRING });
        int count = 0;
        for (SearchHistory history : mDataInteractor.getSearchHistory()) {
            if (history.searchString.toLowerCase().startsWith(search.toLowerCase())) {
                c.addRow(new Object[]{count, history.searchString});
                count++;
            }
        }
        return c;
    }

    @Override
    public CursorAdapter getCursorAdapter(Context context) {
        final String[] from = new String[] {Constants.SEARCH_STRING};
        final int[] to = new int[] {android.R.id.text1};
        return new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public void addSearchSuggestion(String suggestion) {
        suggestion = suggestion.replaceAll("\\-", "::");
        mDataInteractor.addSearchHistory(suggestion);
    }

    @Override
    public Observable<Release> getAuthorReleases(String pauseId) {
        return Observable.from(internalPerformSearch("author:" + pauseId.toUpperCase(), null, null, 100, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<List<Release>, Observable<Release>>() {
                    @Override
                    public Observable<Release> call(List<Release> releaseList) {
                        return Observable.from(releaseList);
                    }
                });
    }

    private Future<List<Release>> internalPerformSearch(String query, final FragmentInterface listener, final Callback<ArrayList<Release>> callback, final int size, final int offset) {
        final String finalQuery = query.replace("::", "-");
        return mThreadExecutor.submit(new Callable<List<Release>>() {
            @Override
            public List<Release> call() throws Exception {
                List<Release> releaseSearch = mApiInteractor.searchRelease(finalQuery + " AND status:latest", size, offset);
                try {
                    if (callback != null) {
                        if (listener != null) {
                            listener.dismissProgress();
                        }
                        callback.call(new ArrayList<Release>(releaseSearch));
                    } else if (listener != null) {
                        SearchResultFragment fragment = SearchResultFragment.newInstance(finalQuery, new ArrayList<Release>(releaseSearch));
                        listener.dismissProgress();
                        listener.openFragment(fragment, SearchResultFragment.TAG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return releaseSearch;
            }
        });
    }


}

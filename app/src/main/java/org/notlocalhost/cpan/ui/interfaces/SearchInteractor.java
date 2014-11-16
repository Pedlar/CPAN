package org.notlocalhost.cpan.ui.interfaces;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;

import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.metacpan.models.Release;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import rx.Observable;

/**
 * Created by pedlar on 10/11/14.
 */
public interface SearchInteractor {
    public Future<List<Release>> getMoreResults(final String query, int size, int offset);
    public Future<List<Release>> performSearch(String query, FragmentInterface listener, Callback<ArrayList<Release>> cb);
    public Observable<SearchModel> getReleaseInformation(final List<Release> releeaseList);
    public Cursor getSearchSuggestions(String search);
    public CursorAdapter getCursorAdapter(Context context);
    public void addSearchSuggestion(String suggestion);
    public Observable<Release> getAuthorReleases(String pauseId);
}

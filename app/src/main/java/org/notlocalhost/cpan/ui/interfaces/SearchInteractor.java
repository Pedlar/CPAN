package org.notlocalhost.cpan.ui.interfaces;

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
    public Future<List<Release>> performSearch(String query, FragmentInterface listener);
    public Observable<SearchModel> getReleaseInformation(final List<Release> releeaseList);
}

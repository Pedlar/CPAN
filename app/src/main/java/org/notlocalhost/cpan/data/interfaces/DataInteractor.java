package org.notlocalhost.cpan.data.interfaces;

import org.notlocalhost.cpan.data.SearchHistory;

import java.util.ArrayList;

/**
 * Created by pedlar on 11/2/14.
 */
public interface DataInteractor {
    public ArrayList<SearchHistory> getSearchHistory();
    public void addSearchHistory(String search);
}

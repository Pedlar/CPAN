package org.notlocalhost.cpan.data;

import org.notlocalhost.cpan.data.interfaces.DataInteractor;
import org.notlocalhost.cpan.data.interfaces.SqliteService;
import org.notlocalhost.cpan.data.models.SearchModel;

import java.util.ArrayList;

/**
 * Created by pedlar on 11/2/14.
 */
public class DataInteractorImpl implements DataInteractor {
    private SqliteService mSqliteService;
    public DataInteractorImpl(SqliteService sqliteService) {
        mSqliteService = sqliteService;
    }

    public ArrayList<SearchHistory> getSearchHistory() {
        ArrayList<SearchHistory> list = mSqliteService.getSearchHistory();
        if(list == null) {
            list = new ArrayList<SearchHistory>();
        }
        return list;
    }

    public void addSearchHistory(String search) {
        ArrayList<SearchHistory> historyList = getSearchHistory();
        for(SearchHistory history : historyList) {
            if(history.searchString.equalsIgnoreCase(search)) {
                return;
            }
        }
        mSqliteService.addSearchHistory(search);
    }
}

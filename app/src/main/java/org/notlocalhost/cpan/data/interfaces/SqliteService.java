package org.notlocalhost.cpan.data.interfaces;

import org.notlocalhost.cpan.Constants;
import org.notlocalhost.cpan.data.SearchHistory;
import org.notlocalhost.sqliteadapter.annotations.Column;
import org.notlocalhost.sqliteadapter.annotations.INSERT;
import org.notlocalhost.sqliteadapter.annotations.QUERY;
import org.notlocalhost.sqliteadapter.annotations.TableName;

import java.util.ArrayList;

/**
 * Created by pedlar on 11/2/14.
 */
public interface SqliteService {

    @TableName(Constants.TABLE_NAME)
    @QUERY
    public ArrayList<SearchHistory> getSearchHistory();

    @TableName(Constants.TABLE_NAME)
    @INSERT
    public void addSearchHistory(@Column(name = Constants.SEARCH_STRING) String searchString);

}

package org.notlocalhost.cpan.data;

import org.notlocalhost.cpan.Constants;
import org.notlocalhost.sqliteadapter.Schema;
import org.notlocalhost.sqliteadapter.annotations.Column;
import org.notlocalhost.sqliteadapter.annotations.Table;
import org.notlocalhost.sqliteadapter.annotations.TableName;

/**
 * Created by pedlar on 11/2/14.
 */
@TableName(Constants.TABLE_NAME)
public class SearchHistory implements Schema {
    @Column(name = Constants.SEARCH_STRING)
    public String searchString;
}

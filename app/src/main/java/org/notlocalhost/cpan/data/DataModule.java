package org.notlocalhost.cpan.data;


import android.content.Context;

import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.data.interfaces.DataInteractor;
import org.notlocalhost.cpan.data.interfaces.SqliteService;
import org.notlocalhost.cpan.ui.activities.MainActivity;
import org.notlocalhost.cpan.ui.fragments.SearchFragment;
import org.notlocalhost.sqliteadapter.SQLiteAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    SQLiteAdapter sqLiteAdapter;
    Context applicationContext;
    public DataModule(Context application) {
        applicationContext = application;
        sqLiteAdapter = new SQLiteAdapter.Builder(application)
                .setSchemas(SearchHistory.class)
                .setDatabaseName("cpan_db.db")
                .build();
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(10);
    }

    @Provides
    @Singleton
    public ApiInteractor provideApiInteractor() {
        return new ApiInteractorImpl(applicationContext);
    }

    @Provides
    @Singleton
    public DataInteractor provideDataInteractor(SqliteService sqliteService) {
        return new DataInteractorImpl(sqliteService);
    }

    @Provides
    @Singleton
    public SqliteService provideSqliteService() {
        return sqLiteAdapter.create(SqliteService.class);
    }

}

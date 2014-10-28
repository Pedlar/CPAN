package org.notlocalhost.cpan.data;

import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.metacpan.MetaCpan;
import org.notlocalhost.metacpan.PodOutput;
import org.notlocalhost.metacpan.models.Author;
import org.notlocalhost.metacpan.models.Module;
import org.notlocalhost.metacpan.models.Release;

import java.util.List;

import retrofit.RestAdapter;

/**
 * Created by pedlar on 10/11/14.
 */
public class ApiInteractorImpl implements ApiInteractor {
    MetaCpan mMetaCpan;

    public ApiInteractorImpl() {
        mMetaCpan = MetaCpan.instance();
        mMetaCpan.setLogLevel(RestAdapter.LogLevel.BASIC);
    }

    @Override
    public List<Release> searchRelease(String query, int size, int offset) {
        return mMetaCpan.searchReleases(query, size, offset);
    }

    @Override
    public Author getAuthor(String author) {
        return mMetaCpan.getAuthor(author);
    }

    @Override
    public Module getModule(String module) {
        return mMetaCpan.getModule(module);
    }

    @Override
    public String getPod(String module) {
        module = module.replace("-", "::");
        return mMetaCpan.getPod(module, PodOutput.HTML);
    }

    @Override
    public Release getRelease(String releaseName) {
        return mMetaCpan.getRelease(releaseName);
    }
}

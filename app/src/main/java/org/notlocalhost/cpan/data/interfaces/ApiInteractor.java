package org.notlocalhost.cpan.data.interfaces;

import org.notlocalhost.cpan.data.ApiInteractorImpl;
import org.notlocalhost.metacpan.models.Author;
import org.notlocalhost.metacpan.models.Module;
import org.notlocalhost.metacpan.models.Release;

import java.util.List;

/**
 * Created by pedlar on 10/11/14.
 */
public interface ApiInteractor {

    public List<Release> searchRelease(String query, int size, int offset);
    public Author getAuthor(String author);
    public Module getModule(String module);
    public String getPod(String module);
    public Release getRelease(String releaseName);
}

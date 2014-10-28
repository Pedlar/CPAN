package org.notlocalhost.cpan.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.notlocalhost.metacpan.models.Author;
import org.notlocalhost.metacpan.models.Module;
import org.notlocalhost.metacpan.models.Release;

/**
 * Created by pedlar on 10/11/14.
 */
public class SearchModel implements Parcelable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchModel that = (SearchModel) o;

        if (!release.getName().equals(that.release.getName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return release.getName().hashCode();
    }

    public SearchModel() {

    }
    public Release release;
    public Author author;
    public Module module;
    public float rating;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(release);
        dest.writeSerializable(author);
        dest.writeSerializable(module);
        dest.writeFloat(rating);
    }

    public static final Creator<SearchModel> CREATOR = new Creator<SearchModel>() {
        @Override
        public SearchModel createFromParcel(Parcel source) {
            SearchModel model = new SearchModel();
            model.release = (Release)source.readSerializable();
            model.author = (Author)source.readSerializable();
            model.module = (Module)source.readSerializable();
            model.rating = source.readFloat();
            return model;
        }

        @Override
        public SearchModel[] newArray(int size) {
            return new SearchModel[size];
        }
    };
}

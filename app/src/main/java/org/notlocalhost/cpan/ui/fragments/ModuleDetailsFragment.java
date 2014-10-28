package org.notlocalhost.cpan.ui.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;
import org.notlocalhost.metacpan.models.Author;
import org.notlocalhost.metacpan.models.Release;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pedlar on 10/12/14.
 */
public class ModuleDetailsFragment extends BaseFragment {
    public static final String TAG = "ModuleDetailsFragment";
    private static final String DIST = "distribution";
    private SearchModel searchModel;

    @InjectView(R.id.module_name)
    public TextView moduleName;

    @InjectView(R.id.author_name)
    public TextView authorName;

    @InjectView(R.id.author_gravatar)
    public ImageView authorGravatar;

    @InjectView(R.id.ratingBar)
    public RatingBar ratingBar;

    @InjectView(R.id.pod_view)
    public WebView podView;

    @Inject
    ApiInteractor mApiInteractor;

    public ModuleDetailsFragment() {

    }

    public static ModuleDetailsFragment newInstance(SearchModel searchModel) {
        ModuleDetailsFragment fragment = new ModuleDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(DIST, searchModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        if (getArguments() != null) {
            searchModel = getArguments().getParcelable(DIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_module_details, container, false);
        ButterKnife.inject(this, view);

        String moduleNameText = searchModel.release.getDistribution().replace("-", "::");
        moduleName.setText(moduleNameText);
        authorName.setText(searchModel.author.getPauseId());
        ratingBar.setRating(searchModel.rating);
        Picasso.with(getActivity()).load(searchModel.author.getGravatarUrl()).into(authorGravatar);

        podView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView podView, String url) {
                // TODO: One day parse these so they can be opened in here
                return false;
            }
        });

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                return mApiInteractor.getPod(params[0]);
            }

            @Override
            protected void onPostExecute(String htmlPod) {
                String formattedPod = "<html><head><link href=\"style/pod.css\" type=\"text/css\" rel=\"stylesheet\"/></head><body class=\"pod\">"
                        + htmlPod
                        + "</body></html>";

                podView.loadDataWithBaseURL("file:///android_asset/web/pod/", formattedPod, "text/html", "UTF-8", null);
            }
        }.execute(searchModel.release.getDistribution());

        return view;
    }
}

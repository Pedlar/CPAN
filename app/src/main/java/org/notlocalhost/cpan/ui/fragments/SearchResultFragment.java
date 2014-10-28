package org.notlocalhost.cpan.ui.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;
import org.notlocalhost.cpan.ui.itemanimator.SlideInLeftItemAnimator;
import org.notlocalhost.metacpan.models.Release;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import rx.Subscriber;
import rx.functions.Action1;


/**
 * Created by pedlar on 10/11/14.
 */
public class SearchResultFragment extends BaseFragment {
    public static final String TAG = "SearchResultFragment";
    private static final String SEARCH_RESULTS = "search_results";
    private static final String SEARCH_STRING = "search_string";

    @InjectView(R.id.recycler_view)
    RecyclerView mRecycleView;

    @Inject
    SearchInteractor mSearchInteractor;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<SearchModel> searchModels = new ArrayList<SearchModel>();

    private ArrayList<Release> mReleaseList;

    private FetchMoreResults fetchMoreResultsTask;

    private boolean safeToGetMore = false;

    private String mSearchString;

    public SearchResultFragment() {

    }

    private class ReleaseSubscriber extends Subscriber<SearchModel> {
        @Override
        public void onCompleted() {
            safeToGetMore = true;
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(SearchModel searchModel) {
            if(!searchModels.contains(searchModel)) {
                searchModels.add(searchModel);
                mAdapter.notifyItemInserted((searchModels.size() - 1));
            }
        }
    };

    public static SearchResultFragment newInstance(String searchString, ArrayList<Release> searchResults) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putSerializable(SEARCH_RESULTS, searchResults);
        args.putString(SEARCH_STRING, searchString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        if (getArguments() != null) {
            mReleaseList = (ArrayList<Release>)getArguments().getSerializable(SEARCH_RESULTS);
            mSearchString = getArguments().getString(SEARCH_STRING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        ButterKnife.inject(this, view);

        mRecycleView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public int scrollVerticallyBy(int dy, android.support.v7.widget.RecyclerView.Recycler recycler, android.support.v7.widget.RecyclerView.State state) {
                int x = super.scrollVerticallyBy(dy, recycler, state);
                int lastItem = findLastVisibleItemPosition() + 1;
                if(x == 0) {
                    if (lastItem >= mReleaseList.size()) {
                        if (safeToGetMore && (fetchMoreResultsTask == null || fetchMoreResultsTask.getStatus() == AsyncTask.Status.FINISHED)) {
                            safeToGetMore = false;
                            fetchMoreResultsTask = new FetchMoreResults();
                            fetchMoreResultsTask.execute(mReleaseList.size());
                        }
                    }
                }
                return x;
            }
        };
        mRecycleView.setLayoutManager(mLayoutManager);

        mAdapter = new ListViewAdapter();
        mRecycleView.setAdapter(mAdapter);

        mRecycleView.setItemAnimator(new SlideInLeftItemAnimator(mRecycleView));

        if(mReleaseList.size() != searchModels.size()) {
            mSearchInteractor.getReleaseInformation(mReleaseList).subscribe(new ReleaseSubscriber());
        }

        return view;
    }

    public class ListViewAdapter extends RecyclerView.Adapter<ListViewItem> {

        public SearchModel getItem(int position) {
            return searchModels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return searchModels.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return searchModels.size();
        }


        @Override
        public ListViewItem onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_item, parent, false);

            ListViewItem vh = new ListViewItem(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ListViewItem holder, int position) {
            SearchModel model = getItem(position);
            holder.setModel(model);
        }

    }

    public class ListViewItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ListViewItem(View view) {
            super(view);
            ButterKnife.inject(this, view);
            cardView.setOnClickListener(this);
        }

        @InjectView(R.id.card_view)
        public CardView cardView;

        @InjectView(R.id.module_name)
        public TextView moduleName;

        @InjectView(R.id.author_name)
        public TextView authorName;

        @InjectView(R.id.author_gravatar)
        public ImageView authorGravatar;

        @InjectView(R.id.ratingBar)
        public RatingBar ratingBar;

        SearchModel model;

        public void setModel(SearchModel model) {
            this.model = model;
            String moduleNameString = model.release.getDistribution().replace("-", "::");
            moduleName.setText(moduleNameString);
            authorName.setText(model.author.getPauseId());
            ratingBar.setRating(model.rating);
            Picasso.with(getActivity()).load(model.author.getGravatarUrl()).into(authorGravatar);
        }

        @Override
        public void onClick(View v) {
            if(this.model != null && mListener != null) {
                mListener.openFragment(ModuleDetailsFragment.newInstance(model), model.release.getDistribution());
            }
        }
    }

    private class FetchMoreResults extends AsyncTask<Integer, Void, List<Release>> {

        @Override
        protected List<Release> doInBackground(Integer... params) {
            try {
                return mSearchInteractor.getMoreResults(mSearchString, 10, params[0]).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Release> releaseList) {
            mReleaseList.addAll(releaseList);
            mSearchInteractor.getReleaseInformation(releaseList).subscribe(new ReleaseSubscriber());
        }
    }
}

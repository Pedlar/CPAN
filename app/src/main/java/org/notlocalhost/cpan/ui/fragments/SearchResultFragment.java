package org.notlocalhost.cpan.ui.fragments;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.notlocalhost.cpan.Constants;
import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.interfaces.Callback;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;
import org.notlocalhost.cpan.ui.itemanimator.SlideInLeftItemAnimator;
import org.notlocalhost.cpan.view.NavigationDrawable;
import org.notlocalhost.metacpan.models.Release;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.Subscription;


/**
 * Created by pedlar on 10/11/14.
 */
public class SearchResultFragment extends BaseFragment {
    public static final String TAG = "SearchResultFragment";
    private static final String SEARCH_RESULTS = "search_results";
    private static final String SEARCH_STRING = "search_string";

    @InjectView(R.id.recycler_view)
    RecyclerView mRecycleView;

    Toolbar mToolbar;

    SearchView mSearchView;

    @Inject
    SearchInteractor mSearchInteractor;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<SearchModel> searchModels = new ArrayList<SearchModel>();

    private ArrayList<Release> mReleaseList;

    private FetchMoreResults fetchMoreResultsTask;

    private boolean safeToGetMore = false;

    private String mSearchString;

    private Subscription mModelSubscription;

    private CursorAdapter mSearchAdapter;

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
    public void onDestroy() {
        super.onDestroy();
        if(mModelSubscription != null &&
                !mModelSubscription.isUnsubscribed()) {
            mModelSubscription.unsubscribe();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        ButterKnife.inject(this, view);

        if(!mListener.getNavigationDrawable().isRunning()) {
            mListener.getNavigationDrawable().animateIconState(NavigationDrawable.IconState.BURGER, false);
        }
        
        mToolbar = mListener.getToolbar();
        mToolbar.setVisibility(View.VISIBLE);
        if (mToolbar.getMenu().findItem(R.id.action_search) == null) {
            mToolbar.inflateMenu(R.menu.search_menu);
        }
        mSearchView = (SearchView)mToolbar.getMenu().findItem(R.id.action_search).getActionView();

        setupSearchView();

        setupRecyclerView();

        if(mReleaseList.size() != searchModels.size()) {
            getReleaseInformation(mReleaseList);
        }


        return view;
    }

    private void setupRecyclerView() {
        mRecycleView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
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
    }

    private void setupSearchView() {
        mSearchView.setBackgroundColor(getResources().getColor(android.R.color.white));
        mSearchView.setQueryHint(getResources().getString(R.string.search));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(s.length() > 0) {
                    Future future = mSearchInteractor.performSearch(s, mListener, new Callback<ArrayList<Release>>() {
                        @Override
                        public void call(final ArrayList<Release> releaseList) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mReleaseList.clear();
                                    searchModels.clear();
                                    mAdapter.notifyDataSetChanged();
                                    mReleaseList.addAll(releaseList);
                                    getReleaseInformation(releaseList);
                                }
                            });
                        }
                    });
                } else {
                    return true; // Keep the keyboard open.
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Cursor c = mSearchInteractor.getSearchSuggestions(s);
                mSearchAdapter.changeCursor(c);
                return false;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor c = (Cursor)mSearchAdapter.getItem(i);
                String query = c.getString(c.getColumnIndex(Constants.SEARCH_STRING));
                mSearchView.setQuery(query, true);
                return true;
            }
        });

        mSearchAdapter = mSearchInteractor.getCursorAdapter(getActivity());
        mSearchView.setSuggestionsAdapter(mSearchAdapter);
    }

    public void getReleaseInformation(List<Release> releaseList) {
        if(mModelSubscription != null && !mModelSubscription.isUnsubscribed()) {
            mModelSubscription.unsubscribe();
        }
        mModelSubscription = mSearchInteractor.getReleaseInformation(releaseList).subscribe(new ReleaseSubscriber());
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
            ViewCompat.setTransitionName(cardView, model.release.getDistribution());
            String moduleNameString = model.release.getDistribution().replace("-", "::");
            moduleName.setText(moduleNameString);
            authorName.setText(model.author.getPauseId());
            ratingBar.setRating(model.rating);
            Picasso.with(getActivity()).load(model.author.getGravatarUrl()).into(authorGravatar);
        }

        @Override
        public void onClick(View v) {
            if(this.model != null && mListener != null) {
                mSearchInteractor.addSearchSuggestion(model.release.getDistribution());
                ((NavigationDrawable)mListener.getToolbar().getNavigationIcon()).animateIconState(NavigationDrawable.IconState.ARROW, true);
                mListener.openFragment(ModuleDetailsFragment.newInstance(model), model.release.getDistribution(), v, model.release.getDistribution());
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
            getReleaseInformation(releaseList);
        }
    }
}

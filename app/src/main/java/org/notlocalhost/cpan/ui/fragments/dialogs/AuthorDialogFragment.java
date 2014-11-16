package org.notlocalhost.cpan.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.fragments.ModuleDetailsFragment;
import org.notlocalhost.cpan.ui.interfaces.FragmentInterface;
import org.notlocalhost.cpan.ui.interfaces.SearchInteractor;
import org.notlocalhost.cpan.ui.itemanimator.SlideInLeftItemAnimator;
import org.notlocalhost.metacpan.models.Author;
import org.notlocalhost.metacpan.models.Release;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;

/**
 * Created by pedlar on 11/15/14.
 */
public class AuthorDialogFragment extends DialogFragment {
    public static final String TAG = "AuthorDialogFragment";

    Author mAuthor;

    @InjectView(R.id.author_gravatar)
    ImageView mGravatar;

    @InjectView(R.id.author_name)
    TextView mAuthorName;

    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Inject
    SearchInteractor mSearchInteractor;

    private RecyclerView.LayoutManager mLayoutManager;
    private ReleaseListAdapter mAdapter;

    private List<Release> mReleaseList = new ArrayList<Release>();

    private FragmentInterface mListener;

    public AuthorDialogFragment() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementReturnTransition(new ChangeImageTransform());
            setSharedElementEnterTransition(new ChangeImageTransform());
            setAllowEnterTransitionOverlap(true);
            setAllowReturnTransitionOverlap(true);
        }
    }

    public static AuthorDialogFragment newInstance(Author author) {
        AuthorDialogFragment fragment = new AuthorDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("author", author);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Injector.inject(this);
        Bundle args = getArguments();
        if(args != null) {
            if(args.containsKey("author")) {
                mAuthor = (Author)args.getSerializable("author");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View view = inflater.inflate(R.layout.author_dialog, container, true);
        ButterKnife.inject(this, view);
        Picasso.with(getActivity()).load(mAuthor.getGravatarUrl()).into(mGravatar);
        mAuthorName.setText(mAuthor.getName());
        ViewCompat.setTransitionName(mGravatar, mAuthor.getName());

        setupRecyclerView();

        mSearchInteractor.getAuthorReleases(mAuthor.getPauseId()).subscribe(new Action1<Release>() {
            @Override
            public void call(Release release) {
                mReleaseList.add(release);
                mAdapter.notifyItemInserted(mReleaseList.size() - 1);
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInterface");
        }
    }

    private void setupRecyclerView() {
        //mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int x = super.scrollVerticallyBy(dy, recycler, state);
                int lastItem = findLastVisibleItemPosition() + 1;
                if(x == 0) {
                    if (lastItem >= mReleaseList.size()) {
                        // No-Op, we don't need to page this yet.
                    }
                }
                return x;
            }
        };

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ReleaseListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new SlideInLeftItemAnimator(mRecyclerView));
    }

    public class ReleaseListAdapter extends RecyclerView.Adapter<ReleaseListItem> {

        public Release getItem(int position) {
            return mReleaseList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mReleaseList.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return mReleaseList.size();
        }


        @Override
        public ReleaseListItem onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.author_dialog_release_item, parent, false);

            ReleaseListItem vh = new ReleaseListItem(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ReleaseListItem holder, int position) {
            Release model = getItem(position);
            holder.setModel(model);
        }

    }

    public class ReleaseListItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ReleaseListItem(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
        }

        @InjectView(R.id.release_name)
        public TextView releaseName;

        @InjectView(R.id.release_abstract)
        public TextView releaseAbstract;

        Release release;

        public void setModel(Release release) {
            this.release = release;

            releaseName.setText(release.getName());
            releaseAbstract.setText(release.getAbstract());
        }

        @Override
        public void onClick(View v) {
            if(this.release != null && mListener != null) {
                mSearchInteractor.addSearchSuggestion(release.getDistribution());
                SearchModel model = new SearchModel();
                model.release = release;
                model.author = mAuthor;
                model.rating = 0f;
                ViewCompat.setTransitionName(v, release.getDistribution());
                mListener.openFragment(ModuleDetailsFragment.newInstance(model), release.getDistribution(), v, release.getDistribution());
                AuthorDialogFragment.this.dismiss();
            }

        }
    }
}

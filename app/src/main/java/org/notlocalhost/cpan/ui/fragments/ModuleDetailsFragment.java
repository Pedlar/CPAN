package org.notlocalhost.cpan.ui.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.squareup.picasso.Picasso;

import org.notlocalhost.cpan.Injector;
import org.notlocalhost.cpan.R;
import org.notlocalhost.cpan.data.interfaces.ApiInteractor;
import org.notlocalhost.cpan.data.models.SearchModel;
import org.notlocalhost.cpan.ui.fragments.dialogs.AuthorDialogFragment;
import org.notlocalhost.cpan.view.ObservableWebView;

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

    @InjectView(R.id.author_container)
    public LinearLayout mAuthorContainer;

    @InjectView(R.id.pod_view)
    public ObservableWebView podView;

    @InjectView(R.id.header_container)
    CardView mHeaderContainer;

    @Inject
    ApiInteractor mApiInteractor;

    private int mLastScrollPosition;

    private float mHeaderOriginalY;

    private ShowcaseView mScv;

    public ModuleDetailsFragment() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = new ChangeBounds();
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    showShowcaseView();
                }
                @Override public void onTransitionStart(Transition transition) { }
                @Override public void onTransitionCancel(Transition transition) {}
                @Override public void onTransitionPause(Transition transition) {}
                @Override public void onTransitionResume(Transition transition) {}
            });

            setSharedElementReturnTransition(new ChangeBounds());
            setSharedElementEnterTransition(transition);
            setAllowEnterTransitionOverlap(true);
            setAllowReturnTransitionOverlap(true);
        }
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
        ViewCompat.setTransitionName(mHeaderContainer, searchModel.release.getDistribution());

        mListener.toggleTopFab(true);

        String moduleNameText = searchModel.release.getDistribution().replace("-", "::");
        moduleName.setText(moduleNameText);
        authorName.setText(searchModel.author.getPauseId());
        ratingBar.setRating(searchModel.rating);
        Picasso.with(getActivity()).load(searchModel.author.getGravatarUrl()).into(authorGravatar);

        ViewCompat.setTransitionName(authorGravatar, searchModel.author.getName());
        mAuthorContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorDialogFragment dialogFragment = AuthorDialogFragment.newInstance(searchModel.author);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addSharedElement(authorGravatar, searchModel.author.getName());

                dialogFragment.show(ft, AuthorDialogFragment.TAG);
            }
        });

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

        podView.setOnScrollListener(new ObservableWebView.OnScrollListener() {
            @Override
            public void onScroll(int v, int h) {
                int delta = v - mLastScrollPosition;
                if (mLastScrollPosition > v) { // Scroll Up
                    mHeaderContainer.setY(mHeaderContainer.getY() - delta);
                    float bottomOfHeaderY = mHeaderContainer.getY() + mHeaderContainer.getMeasuredHeight();
                    if (podView.getY() <= bottomOfHeaderY) {
                        float setY = podView.getY() - delta;
                        if (setY > bottomOfHeaderY) {
                            setY = bottomOfHeaderY;
                        }
                        podView.setY(setY);
                    }
                } else { // Scroll Down
                    delta = delta * -1;
                    mHeaderContainer.setY(mHeaderContainer.getY() + delta);
                    if (podView.getY() >= mHeaderOriginalY) {
                        float setY = podView.getY() + delta;
                        if (setY < mHeaderOriginalY) {
                            setY = mHeaderOriginalY;
                        }
                        podView.setY(setY);
                    }
                }
                mLastScrollPosition = v;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        if(mScv != null && mScv.isShown()) {
            mScv.hide();
            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated (final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mHeaderOriginalY = mHeaderContainer.getY();
                float newPodHeight = podView.getMeasuredHeight() + mHeaderContainer.getMeasuredHeight();

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) podView.getLayoutParams();
                layoutParams.height = (int)newPodHeight;
                podView.setLayoutParams(layoutParams);

                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            showShowcaseView();
        }
    }

    private void showShowcaseView() {
        if (getActivity() != null) {
            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 60)).intValue();
            lps.setMargins(margin, margin, margin, margin);

            ViewTarget authorTarget = new ViewTarget(authorGravatar);
            mScv = new ShowcaseView.Builder(getActivity(), true)
                    .setContentTitle("Get Author Information")
                    .setContentText("Touch the authors Gravatar to see more from this author.")
                    .singleShot(R.id.author_gravatar)
                    .setTarget(authorTarget)
                    .hideOnTouchOutside()
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            if (podView != null) {
                                podView.setAlpha(1);
                            }
                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
                            if (podView != null) {
                                podView.setAlpha(0.5f);
                            }
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        }
                    })
                    .build();
            mScv.setButtonPosition(lps);
        }
    }
}
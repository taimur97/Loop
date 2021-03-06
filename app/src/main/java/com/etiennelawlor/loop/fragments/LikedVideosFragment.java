package com.etiennelawlor.loop.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideosCollection;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.VideoLikedEvent;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.squareup.okhttp.ResponseBody;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class LikedVideosFragment extends BaseFragment implements VideosAdapter.OnItemClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Member Variables
    @Bind(R.id.videos_rv)
    RecyclerView mVideosRecyclerView;
    @Bind(android.R.id.empty)
    View mEmptyView;
    @Bind(R.id.empty_tv)
    TextView mEmptyTextView;
    @Bind(R.id.loading_iv)
    LoadingImageView mLoadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout mErrorLinearLayout;
    @Bind(R.id.error_tv)
    TextView mErrorTextView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
//    @Bind(R.id.search_view_widget)
//    SearchViewWidget mSearchViewWidget;

    private boolean mIsLastPage = false;
    private int mCurrentPage = 1;
    private int mSelectedSortByKey = 0;
    private int mSelectedSortOrderKey = 1;
    private boolean mIsLoading = false;
    private String mSortByValue = "date";
    private String mSortOrderValue = "desc";
    private VideosAdapter mVideosAdapter;
    private String mQuery;
    private LinearLayoutManager mLayoutManager;
    private VimeoService mVimeoService;
    private VideoLikedEvent mVideoLikedEvent;
    // endregion

    // region Listeners
    private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if (!mIsLoading && !mIsLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        }
    };

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        mErrorLinearLayout.setVisibility(View.GONE);
        mLoadingImageView.setVisibility(View.VISIBLE);

        Call findLikedVideosCall = mVimeoService.findLikedVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findLikedVideosCall);
        findLikedVideosCall.enqueue(mFindVideosFirstFetchCallback);
    }

    private View.OnClickListener mReloadOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCurrentPage -= 1;
            mVideosAdapter.addLoading();
            loadMoreItems();
        }
    };
    // endregion

    // region Callbacks
    private Callback<VideosCollection> mFindVideosFirstFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response) {
            Timber.d("onResponse()");
            mLoadingImageView.setVisibility(View.GONE);
            mIsLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            mVideosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                mVideosAdapter.addLoading();
                            } else {
                                mIsLastPage = true;
                            }
                        }
                    }
                } else {
                    ResponseBody responseBody = response.errorBody();
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        String message = rawResponse.message();
                        int code = rawResponse.code();
                        Timber.d("onResponse() : message - " + message);
                        Timber.d("onResponse() : code - " + code);

                        switch (code) {
                            case 500:
                                mErrorTextView.setText("Can't load data.\nCheck your network connection.");
                                mErrorLinearLayout.setVisibility(View.VISIBLE);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            if (mVideosAdapter.isEmpty()) {
                mEmptyTextView.setText(getString(R.string.likes_empty_prompt));
                Drawable drawable = getResources().getDrawable(R.drawable.ic_likes_large);
                DrawableCompat.setTint(drawable, getResources().getColor(R.color.grey_500));
                mEmptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.d("onFailure()");

            if (t != null) {
                Throwable cause = t.getCause();
                String message = t.getMessage();

                if (cause != null) {
                    Timber.e("failure() : cause.toString() -" + cause.toString());
                }

                if (!TextUtils.isEmpty(message)) {
                    Timber.e("failure() : message - " + message);
                }

                t.printStackTrace();

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    mIsLoading = false;
                    mLoadingImageView.setVisibility(View.GONE);

                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
                        mIsLoading = false;
                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }

        }
    };

    private Callback<VideosCollection> mFindVideosNextFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response) {
            Timber.d("onResponse()");
            mVideosAdapter.removeLoading();
            mIsLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            mVideosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                mVideosAdapter.addLoading();
                            } else {
                                mIsLastPage = true;
                            }
                        }
                    }
                } else {
                    ResponseBody responseBody = response.errorBody();
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        String message = rawResponse.message();
                        int code = rawResponse.code();
                        Timber.d("onResponse() : message - " + message);
                        Timber.d("onResponse() : code - " + code);

                        switch (code) {
                            case 500:
                                Timber.e("Display error message in place of load more");
//                                mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                                mErrorLinearLayout.setVisibility(View.VISIBLE);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.d("onFailure()");

            mVideosAdapter.removeLoading();

            if (t != null) {
                Throwable cause = t.getCause();
                String message = t.getMessage();

                if (cause != null) {
                    Timber.e("failure() : cause.toString() -" + cause.toString());
                }

                if (!TextUtils.isEmpty(message)) {
                    Timber.e("failure() : message - " + message);
                }

                t.printStackTrace();

                if (t instanceof SocketTimeoutException) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            String.format("message - %s", message),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("Reload", mReloadOnClickListener)
//                                .setActionTextColor(Color.RED)
                            .show();
                } else if (t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");

                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "Can't load data. Check your network connection.",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("Reload", mReloadOnClickListener)
//                                .setActionTextColor(Color.RED)
                            .show();

//                    mIsLoading = false;
//                    mProgressBar.setVisibility(View.GONE);

//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
//                        mIsLoading = false;
                        mVideosAdapter.removeLoading();
                    }
                }
            }
        }
    };
    // endregion

    // region Constructors
    public static LikedVideosFragment newInstance() {
        LikedVideosFragment fragment = new LikedVideosFragment();
        return fragment;
    }

    public static LikedVideosFragment newInstance(Bundle extras) {
        LikedVideosFragment fragment = new LikedVideosFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public LikedVideosFragment() {
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mQuery = getArguments().getString("query");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        setHasOptionsMenu(true);
        BusProvider.get().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_liked_videos, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Likes");

        mLayoutManager = new LinearLayoutManager(getActivity());
        mVideosRecyclerView.setLayoutManager(mLayoutManager);
        mVideosAdapter = new VideosAdapter();
        mVideosAdapter.setOnItemClickListener(this);

        mVideosRecyclerView.setItemAnimator(new SlideInUpAnimator());
        mVideosRecyclerView.setAdapter(mVideosAdapter);

        // Pagination
        mVideosRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

        Call findLikedVideosCall = mVimeoService.findLikedVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findLikedVideosCall);
        findLikedVideosCall.enqueue(mFindVideosFirstFetchCallback);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mVideoLikedEvent != null){
            refreshAdapter();
            mVideoLikedEvent = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mVideosRecyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister Otto Bus
        BusProvider.get().unregister(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
//        mSearchViewWidget.setMenuItem(item);

//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setQueryRefinementEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                showSortDialog();
                break;
            default:
                // do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        if (mSearchViewWidget.isSearchOpen()) {
//            mSearchViewWidget.closeSearch();
//        } else {
//            getActivity().onBackPressed();
////            super.onBackPressed();
//        }
//    }

    // region VideosAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        Video video = mVideosAdapter.getItem(position);
        if (video != null) {
            Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("video", video);
            intent.putExtras(bundle);

            Pair<View, String> p1 = Pair.create((View) view.findViewById(R.id.video_thumbnail_iv), "videoTransition");
//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
//        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");

//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                    p1);


//            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

                startActivity(intent);
        }

    }
    // endregion

    // region Otto Methods
    @Subscribe
    public void onVideoLikedEvent(VideoLikedEvent event) {
        Timber.d("onVideoLikedEvent");

        if (isResumed()) {
            refreshAdapter();
            mVideoLikedEvent = null;
        } else {
            mVideoLikedEvent = event;
        }
    }
    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        mIsLoading = true;

        mCurrentPage += 1;

        Call findLikedVideosCall = mVimeoService.findLikedVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findLikedVideosCall);
        findLikedVideosCall.enqueue(mFindVideosNextFetchCallback);
    }

    private void showSortDialog() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.sort_dialog, null);
        final Spinner sortBySpinner = (Spinner) promptsView.findViewById(R.id.sort_by_s);
        final Spinner sortOrderSpinner = (Spinner) promptsView.findViewById(R.id.sort_order_s);

        String[] mSortByKeysArray = getResources().getStringArray(R.array.likes_sort_by_keys);
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortByKeysArray);
        sortBySpinner.setAdapter(sortByAdapter);

        String[] mSortOrderKeysArray = getResources().getStringArray(R.array.likes_sort_order_keys);
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortOrderKeysArray);
        sortOrderSpinner.setAdapter(sortOrderAdapter);

        sortBySpinner.setSelection(mSelectedSortByKey);
        sortOrderSpinner.setSelection(mSelectedSortOrderKey);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedSortByKey = sortBySpinner.getSelectedItemPosition();
                mSelectedSortOrderKey = sortOrderSpinner.getSelectedItemPosition();

                String[] sortByValues = getResources().getStringArray(R.array.likes_sort_by_values);
                mSortByValue = sortByValues[mSelectedSortByKey];

                String[] sortOrderValues = getResources().getStringArray(R.array.likes_sort_order_values);
                mSortOrderValue = sortOrderValues[mSelectedSortOrderKey];

                mVideosAdapter.clear();

                mLoadingImageView.setVisibility(View.VISIBLE);

                mCurrentPage = 1;

                Call findLikedVideosCall = mVimeoService.findLikedVideos(mQuery,
                        mSortByValue,
                        mSortOrderValue,
                        mCurrentPage,
                        PAGE_SIZE);
                mCalls.add(findLikedVideosCall);
                findLikedVideosCall.enqueue(mFindVideosFirstFetchCallback);

                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.show();
    }

    private void refreshAdapter(){
        mVideosAdapter.clear();

        mLoadingImageView.setVisibility(View.VISIBLE);

        mCurrentPage = 1;

        Call findLikedVideosCall = mVimeoService.findLikedVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findLikedVideosCall);
        findLikedVideosCall.enqueue(mFindVideosFirstFetchCallback);
    }
    // endregion
}

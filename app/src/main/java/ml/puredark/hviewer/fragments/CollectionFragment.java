package ml.puredark.hviewer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.activities.AnimationActivity;
import ml.puredark.hviewer.activities.CollectionActivity;
import ml.puredark.hviewer.adapters.CollectionAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.RuleParser;
import ml.puredark.hviewer.utils.DensityUtil;

public class CollectionFragment extends MyFragment {

    @BindView(R.id.rv_collection)
    PullLoadMoreRecyclerView rvCollection;

    CollectionAdapter adapter;

    private Site site;

    private String keyword = null;
    private int startPage;
    private int currPage;

    public CollectionFragment() {
    }

    public static CollectionFragment newInstance() {
        CollectionFragment fragment = new CollectionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (HViewerApplication.temp instanceof Site)
            site = (Site) HViewerApplication.temp;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);
        ButterKnife.bind(this, rootView);


        List<Collection> collections = new ArrayList<>();
        AbstractDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(dataProvider);
        rvCollection.setAdapter(adapter);

        rvCollection.setLinearLayout();
        rvCollection.setPullRefreshEnable(true);
        rvCollection.getRecyclerView().setClipToPadding(false);
        rvCollection.getRecyclerView().setPadding(0, DensityUtil.dp2px(getActivity(), 8), 0, DensityUtil.dp2px(getActivity(), 8));

        //下拉刷新和加载更多
        rvCollection.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                keyword = null;
                currPage = startPage;
                rvCollection.setRefreshing(true);
                getCollections(null, currPage);
            }

            @Override
            public void onLoadMore() {
                getCollections(keyword, currPage + 1);
            }
        });

        if (site != null) {
            adapter.setCookie(site.cookie);
            Map<String, String> map = RuleParser.parseUrl(site.indexUrl);
            String pageStr = map.get("page");
            try {
                startPage = (pageStr != null) ? Integer.parseInt(pageStr) : 0;
                currPage = startPage;
            } catch (NumberFormatException e) {
                startPage = 0;
                currPage = startPage;
            }
            getCollections(null, startPage);

        }
        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                HViewerApplication.temp = site;
                HViewerApplication.temp2 = collection;
                Intent intent = new Intent(CollectionFragment.this.getContext(), CollectionActivity.class);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                return false;
            }
        });

        return rootView;
    }

    private void getCollections(String keyword, final int page) {
        this.keyword = keyword;
        String chooseUrl = (keyword == null) ? site.indexUrl : site.searchUrl;
        final String url = chooseUrl.replaceAll("\\{page:" + startPage + "\\}", "" + page)
                .replaceAll("\\{keyword:\\}", keyword);
        HViewerHttpClient.get(url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String result) {
                if (page == startPage) {
                    adapter.getDataProvider().clear();
                }
                List<Collection> collections = adapter.getDataProvider().getItems();
                int oldSize = collections.size();
                collections = RuleParser.getCollections(collections, result, site.indexRule, url);
                int newSize = collections.size();
                adapter.notifyDataSetChanged();
                if (newSize > oldSize) {
                    currPage = page;
                    addSearchSuggestions(collections);
                }
                rvCollection.setPullLoadMoreCompleted();
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                ((AnimationActivity)getActivity()).showSnackBar(error.getErrorString());
                rvCollection.setPullLoadMoreCompleted();
            }
        });
    }

    private void addSearchSuggestions(List<Collection> collections){
        for(Collection collection : collections){
            if(collection.tags!=null) {
                for (Tag tag : collection.tags) {
                    HViewerApplication.searchSuggestionHolder.addSearchSuggestion(tag.title);
                }
            }
        }
        HViewerApplication.searchSuggestionHolder.removeDuplicate();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSearch(String keyword) {
        getCollections(keyword, startPage);
    }
}

package ml.puredark.hviewer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.CollectionAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.RuleParser;

public class CollectionFragment extends MyFragment {

    @BindView(R.id.rv_collection)
    RecyclerView rvCollection;

    CollectionAdapter adapter;

    private Site site;

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

        if (site == null) {
            site = new Site(0, "", "", null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);
        ButterKnife.bind(this, rootView);

        List<Collection> collections = new ArrayList<>();
        ListDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(dataProvider);
        rvCollection.setAdapter(adapter);

        getCollections(site);

        return rootView;
    }

    private void getCollections(final Site site){
        HViewerHttpClient.get(site.indexUrl, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String result) {

                List<Collection> collections = RuleParser.getCollections(result, site.rule);
                adapter.getDataProvider().clear();
                adapter.getDataProvider().addAll(collections);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                Toast.makeText(getContext(), error.getErrorString(), Toast.LENGTH_SHORT).show();
            }
        });
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

    }
}

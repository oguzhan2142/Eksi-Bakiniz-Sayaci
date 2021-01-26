package com.example.bkzsayac.ui.home;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bkzsayac.AnalyzePage;
import com.example.bkzsayac.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment
{


    //    private HomeViewModel homeViewModel;
    private List<Data> datas = new ArrayList<>();
    private String[] entries;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
//        View view = inflater.inflate(R.layout.fragment_home, container, false);

//        swipeRefreshLayout = view.findViewById(R.id.swipereflesh);
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */


        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void reflesh()
    {
        new Crawler().execute();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        swipeRefreshLayout = getView().findViewById(R.id.swipereflesh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener()
                {
                    @Override
                    public void onRefresh()
                    {

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
//                        smyUpdateOperation();
                        reflesh();
                    }
                }
        );
        new Crawler().execute();


    }


    private class Crawler extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {


            Document document = null;
            try
            {


                String url = getArguments().getString("link");

                document = Jsoup.connect(url).get();

                Elements elements = document.select("#content-body a");
                if (elements.select("#quick-index-continue-link").size() > 0)
                    elements.remove(elements.select("#quick-index-continue-link").first());
                if (elements.select(".channel-filter-toggle").size() > 0)
                    elements.remove(elements.select(".channel-filter-toggle").first());

                entries = new String[elements.size()];
                int index = 0;
                for (Element e : elements)
                {
                    e.select("small").remove();

                    String href = e.attr("href");
                    String entry = e.text();

                    datas.add(new Data(entry, href));
                    entries[index] = entry;
                    index++;
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            ListView mListView = (ListView) getActivity().findViewById(R.id.listView1);
            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, entries);
            mListView.setAdapter(mArrayAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {

                    Intent intent = new Intent(getActivity(), AnalyzePage.class);
                    intent.putExtra("href", datas.get(position).href);
                    intent.putExtra("entry", datas.get(position).entry);
                    startActivity(intent);
                }
            });
            swipeRefreshLayout.setRefreshing(false);

        }
    }


    class Data
    {
        public String entry;
        public String href;


        public Data(String entry, String href)
        {
            this.entry = entry;
            this.href = href;

        }
    }
}
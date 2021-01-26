package com.example.bkzsayac;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bkzsayac.ui.AnalyzedData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class AnalyzePage extends AppCompatActivity
{

    private final long SLEEP_TIME = 2000L;
    private final String URL = "https://eksisozluk.com";


    private ProgressBar progressBar;
    private TextView progressbarText;
    private AsyncTask<Void, Void, Void> asyncTask;
    private DataStructure analyzedDatas;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_page);
        analyzedDatas = new DataStructure();
        Toolbar toolbar = findViewById(R.id.analyze_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        progressBar = findViewById(R.id.progressbar);
        progressbarText = findViewById(R.id.progress_bar_text);
        setTitle(getIntent().getStringExtra("entry"));
        asyncTask = new CrawEntries(this);
        asyncTask.execute();


    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed()
    {

        asyncTask.cancel(true);
        finish();

        super.onBackPressed();
    }


    private static class CrawEntries extends AsyncTask<Void, Void, Void>
    {

        WeakReference<AnalyzePage> analyzePage;


        public CrawEntries(AnalyzePage analyzePage)
        {
            this.analyzePage = new WeakReference<AnalyzePage>(analyzePage);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {

            int pageCount = 1;

            Intent intent = analyzePage.get().getIntent();

            // ATTENTION: This was auto-generated to handle app links.


//            String appLinkAction = intent.getAction();
            String link = "";
            Uri appLinkData = intent.getData();

            if (appLinkData != null)
            {
                link = appLinkData.toString();
            }




            if (intent.hasExtra("href"))
            {
                link = intent.getStringExtra("href");
                link = analyzePage.get().URL + link;
            }

            if (intent.hasExtra("url"))
            {
                link = intent.getStringExtra("url").toString();
            }

            if (link.contains("?a=popular"))
            {
                link = link.replace("?a=popular", "");
            }

//            Sayfa numarasi aliniyor
            try
            {
                Document document = Jsoup.connect(link).get();
                String pageString = document.select("#topic > div.clearfix.sub-title-container > div.pager").attr("data-pagecount");
                pageCount = Integer.parseInt(pageString.trim());
                analyzePage.get().progressBar.setMax(pageCount - 1);
                analyzePage.get().progressBar.setProgress(0);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            //Veriler aliniyor sayfa sayfa


            for (int i = 1; i <= pageCount; i++)
            {

                String crawlerLink = link + "?p=" + i;

                Document doc;
                try
                {
                    doc = Jsoup.connect(crawlerLink).get();
                } catch (IOException e)
                {
                    e.printStackTrace();
                    i--;
                    try
                    {
                        Thread.sleep(analyzePage.get().SLEEP_TIME);
                    } catch (InterruptedException interruptedException)
                    {
                        interruptedException.printStackTrace();
                    }
                    continue;
                }
                if (analyzePage.get().asyncTask.isCancelled())
                {
                    break;
                }
                analyzePage.get().progressbarText.setText("Veri alınıyor " + i + "/" + pageCount);
                // Konudaki entriler donuyor
                for (Element element : doc.select(".content"))
                {

                    Elements bkzs = element.select(".b");


                    for (Element bkz : bkzs)
                    {
                        String bkzText = bkz.text();
                        String href = bkz.attr("href");

                        if (analyzePage.get().analyzedDatas.containsBkz(bkzText))
                        {
                            AnalyzedData data = analyzePage.get().analyzedDatas.find(bkzText);
                            data.amount += 1;
                        } else
                        {
                            analyzePage.get().analyzedDatas.add(new AnalyzedData(bkzText, analyzePage.get().URL + href));
                        }
                    }
                }
                analyzePage.get().progressBar.setProgress(analyzePage.get().progressBar.getProgress() + 1);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

            String[] sortedTexts = analyzePage.get().analyzedDatas.toSortedArray();


            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(analyzePage.get().getBaseContext(), android.R.layout.simple_list_item_1, android.R.id.text1, sortedTexts);
            ListView listView = analyzePage.get().findViewById(R.id.list);
            listView.setAdapter(mArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(analyzePage.get().analyzedDatas.get(position).link));
                    analyzePage.get().startActivity(intent);
                }
            });
            analyzePage.get().progressBar.setVisibility(View.INVISIBLE);
            analyzePage.get().progressbarText.setVisibility(View.INVISIBLE);

        }

    }


}

package com.example.bkzsayac.ui.link;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bkzsayac.AnalyzePage;
import com.example.bkzsayac.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class LinkFragment extends Fragment
{

    private final long DELAY_TIME_AFTER_TEXT_CHANGED = 1400L;
    private EditText inputfield;
    private TextView titleField;
    private TextView pageCountField;


    private String pastedLink;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        inputfield = root.findViewById(R.id.linkinputfield);
        titleField = root.findViewById(R.id.baslik_ismi);
        pageCountField = root.findViewById(R.id.sayfa_sayisi);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                new CrawInfos(LinkFragment.this).execute(pastedLink);
            }
        };

        inputfield.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                handler.removeCallbacks(runnable);
                pastedLink = s.toString();
                handler.postDelayed(runnable, DELAY_TIME_AFTER_TEXT_CHANGED);
            }
        });

        root.findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clear();
            }
        });

        root.findViewById(R.id.pasteBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paste();
            }
        });

        root.findViewById(R.id.analyzeBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                analyze();
            }
        });


        return root;
    }

    public void clear()
    {
        inputfield.setText("");
    }

    public void paste()
    {
        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager.hasText())
        {
            inputfield.setText(manager.getText());
        }
    }

    public void analyze()
    {


        try
        {
            URL url = new URL(inputfield.getText().toString());
            url.toURI();
            new CrawPasted(this).execute();
        } catch (MalformedURLException | URISyntaxException e)
        {
            inputfield.setText("Link gecerli degil");
            e.printStackTrace();
        }

    }


    private static class CrawPasted extends AsyncTask<Void, Void, Void>
    {
        private String entry;
        private boolean isUrlValid = false;
        private final WeakReference<LinkFragment> linkFragmentWeakReference;

        public CrawPasted(LinkFragment linkFragment)
        {
            this.linkFragmentWeakReference = new WeakReference<>(linkFragment);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {

            try
            {
                if (!linkFragmentWeakReference.get().inputfield.getText().toString().contains("eksisozluk"))
                {
                    isUrlValid = false;
                    return null;
                }
                Document doc = Jsoup.connect(linkFragmentWeakReference.get().inputfield.getText().toString()).get();
                Element element = doc.select("#title span").first();
                entry = element.text();


                isUrlValid = true;


            } catch (IOException e)
            {
                isUrlValid = false;
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            if (!isUrlValid)
                return;

            Intent intent = new Intent(linkFragmentWeakReference.get().getActivity(), AnalyzePage.class);
            intent.putExtra("url", linkFragmentWeakReference.get().inputfield.getText().toString());
            intent.putExtra("entry", entry);
            linkFragmentWeakReference.get().startActivity(intent);
        }
    }


    private static class CrawInfos extends AsyncTask<String, Void, Void>
    {
        String title = "";
        String pageCount = "";
        private final WeakReference<LinkFragment> linkFragmentWeakReference;
        private boolean isUrlValid = false;

        public CrawInfos(LinkFragment linkFragment)
        {
            this.linkFragmentWeakReference = new WeakReference<>(linkFragment);
        }

        protected Void doInBackground(String... strings)
        {

            try
            {

                if (URLUtil.isValidUrl(strings[0]) && strings[0].contains("eksisozluk"))
                    isUrlValid = true;

                if (!isUrlValid)
                    return null;
                Document doc = Jsoup.connect(strings[0]).get();
                title = doc.select("#title span").text();
                pageCount = doc.select("#topic > div.clearfix.sub-title-container > div.pager").attr("data-pagecount");
            } catch (IOException e)
            {
                Log.e("URL HATASI", strings[0]);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if (!isUrlValid)
                return;
            linkFragmentWeakReference.get().titleField.setText(title);
            linkFragmentWeakReference.get().pageCountField.setText(pageCount);
            super.onPostExecute(aVoid);
        }
    }

}

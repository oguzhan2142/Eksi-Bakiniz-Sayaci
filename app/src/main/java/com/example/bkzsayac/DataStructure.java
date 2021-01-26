package com.example.bkzsayac;

import com.example.bkzsayac.ui.AnalyzedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class DataStructure extends ArrayList<AnalyzedData>
{


    public boolean containsBkz(String bkzText)
    {

        for (AnalyzedData data : this)
        {
            if (data.bkzText.equals(bkzText))
                return true;
        }

        return false;
    }


    public AnalyzedData find(String bkzText)
    {
        for (AnalyzedData data : this)
        {
            if (data.bkzText.equals(bkzText))
                return data;

        }
        return null;
    }

    public String[] toSortedArray()
    {
        Collections.sort(this, new Comparator<AnalyzedData>()
        {
            @Override
            public int compare(AnalyzedData o1, AnalyzedData o2)
            {
                if (o1.amount > o2.amount)
                    return -1;
                else if (o1.amount < o2.amount)
                    return 1;
                else
                    return 0;
            }
        });

        String[] result = new String[this.size()];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = get(i).bkzText + " (" + get(i).amount + ")";
        }
        return result;
    }

}


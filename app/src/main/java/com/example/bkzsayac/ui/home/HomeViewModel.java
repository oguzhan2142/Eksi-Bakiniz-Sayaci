package com.example.bkzsayac.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel
{


    public HomeViewModel()
    {

    }

    public LiveData<String> getText()
    {
        return new LiveData<String>()
        {
        };
    }
}
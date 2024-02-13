package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private MutableLiveData<UserDetails> userDetailsLiveData = new MutableLiveData<>();

    public LiveData<UserDetails> getUserDetails() {
        return userDetailsLiveData;
    }

    public void setUserDetails(UserDetails userDetails) {
        userDetailsLiveData.setValue(userDetails);
    }
}

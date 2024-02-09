package com.example.myapplication;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public interface OnAccountSignInListener {
    void onAccountSignIn(GoogleSignInAccount account);
}

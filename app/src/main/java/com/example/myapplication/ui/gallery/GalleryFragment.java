package com.example.myapplication.ui.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.OnAccountSignInListener;
import com.example.myapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView noSongsText;
    private AudioFilesAdapter adapter;
    private String username;
    private String email;
    private String photoUrl;
    private ImageButton accountDetailsButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.recycler_view2);
        noSongsText = view.findViewById(R.id.no_songs_text2);
        accountDetailsButton = view.findViewById(R.id.accountDetailsButton);


        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AudioFilesAdapter();
        recyclerView.setAdapter(adapter);

        accountDetailsButton.setOnClickListener(signInButtonview -> showAccountDetailsPopup(accountDetailsButton));

        // Access signed-in account details from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        username = mainActivity.getSignedInUsername();
        email = mainActivity.getSignedInEmail();
        photoUrl=mainActivity.getSignedInPhoto();

        loadImageWithGlide(photoUrl, accountDetailsButton);
        // Initialize and set data to the adapter, or perform any other fragment-specific logic

        return view;
    }

    private void showAccountDetailsPopup(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.account_details_menu, popupMenu.getMenu());

        Menu menu = popupMenu.getMenu();

        menu.findItem(R.id.menu_user_name).setTitle(username);
        menu.findItem(R.id.menu_user_email).setTitle(email);


        popupMenu.show();
    }

    private void loadImageWithGlide(String imageUrl, ImageButton accountDetailsButton) {
        // Implement your logic to load the image using Glide
        // Replace R.drawable.default_user_image with the default image resource
        Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(accountDetailsButton);
    }

    // You can include other methods or logic specific to the GalleryFragment
}
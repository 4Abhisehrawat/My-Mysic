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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.UserViewModel;

public class GalleryFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView noSongsText;
    private AudioFilesAdapter adapter;
    private String username;
    private String email;
    private String photoUrl;
    private ImageButton accountDetailsButton;
    private UserViewModel userViewModel;

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
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Observe changes to user details LiveData
        userViewModel.getUserDetails().observe(getViewLifecycleOwner(), userDetails -> {
            // Update UI with the new user details
            if (userDetails != null) {
                username = userDetails.getUsername();
                email = userDetails.getEmail();
                photoUrl = userDetails.getPhotoUrl();
                loadImageWithGlide(photoUrl, accountDetailsButton);
            }

        });
        // Initialize and set data to the adapter, or perform any other fragment-specific logic

        return view;
    }


    private void showAccountDetailsPopup(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.account_details_menu, popupMenu.getMenu());

        Menu menu = popupMenu.getMenu();

        menu.findItem(R.id.menu_user_name).setTitle(username != null ? username : "Unknown");
        menu.findItem(R.id.menu_user_email).setTitle(email != null ? email : "Unknown");

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
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


public class GalleryFragment extends Fragment implements OnAccountSignInListener {
    private static final String TAG = "GalleryFragment";
    private ImageButton accountDetailsButton;
    private Drive driveService;
    private RecyclerView recyclerView;
    private TextView noSongsText;
    private AudioFilesAdapter adapter;


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

        // Access signed-in account from MainActivity
        GoogleSignInAccount signedInAccount = ((MainActivity) requireActivity()).getSignedInAccount();
        onAccountSignIn(signedInAccount);  // Handle the account in the fragment

        return view;
    }

    public void setDriveService(Drive driveService) {
        this.driveService = driveService;
        listFiles();
    }

    private void listFiles() {
        try {
            FileList result = driveService.files().list()
                    .setQ("mimeType='audio/*'")
                    .setSpaces("drive")
                    .execute();

            List<File> files = result.getFiles();
            List<AudioFile> audioFiles = new ArrayList<>();

            if (files != null && !files.isEmpty()) {
                for (File file : files) {
                    String fileName = file.getName();
                    String fileId = file.getId();
                    audioFiles.add(new AudioFile(fileName, fileId));
                }
            }

            if (audioFiles.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                noSongsText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noSongsText.setVisibility(View.GONE);
                adapter.setAudioFiles(audioFiles);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error querying files: " + e.getMessage());
        }
    }

    @Override
    public void onAccountSignIn(GoogleSignInAccount account) {
        // Handle the signed-in account in the fragment
        if (account != null) {
            // Update UI or perform actions specific to this fragment when an account is signed in
            // For example, you can access account.getEmail(), account.getDisplayName(), etc.
            // ...

            // Show account details in a toast (you can replace this with your UI logic)
            String accountDetails = "User: " + account.getDisplayName() + "\nEmail: " + account.getEmail();
            Toast.makeText(requireContext(), accountDetails, Toast.LENGTH_SHORT).show();

            // After signing in, you may want to list files from Drive
            if (driveService != null) {
                listFiles();
            }
        }
        else{
            String Failed_Text="Signin Failed";
            Toast.makeText(requireContext(), Failed_Text, Toast.LENGTH_SHORT).show();
        }
    }



    private void showAccountDetailsPopup(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.account_details_menu, popupMenu.getMenu());

        Menu menu = popupMenu.getMenu();

        // Check if the user is signed in
        GoogleSignInAccount signedInAccount = ((MainActivity) requireActivity()).getSignedInAccount();
        if (signedInAccount != null) {
            // If signed in, update the titles with user information
            menu.findItem(R.id.menu_user_name).setTitle(signedInAccount.getDisplayName());
            menu.findItem(R.id.menu_user_email).setTitle(signedInAccount.getEmail());
        } else {
            // If not signed in, use default titles
            menu.findItem(R.id.menu_user_name).setTitle("Default User");
            menu.findItem(R.id.menu_user_email).setTitle("defaultuser@gmail.com");
        }

        popupMenu.show();
    }
}

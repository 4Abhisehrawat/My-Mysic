package com.example.myapplication.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.io.File;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements MusicListAdapter.OnItemClickListener {

    private ArrayList<AudioModel> originalSongsList; // Store the original list of songs

    RecyclerView recyclerView;
    TextView noMusicTextView;
    private final ArrayList<AudioModel> songsList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize your UI components
        recyclerView = root.findViewById(R.id.recycler_view);
        noMusicTextView = root.findViewById(R.id.no_songs_text);

        // ... other UI setup ...

        if (!checkPermission()) {
            requestPermission();
            return root;
        }

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " !=0";

        // Use requireActivity() to get the ContentResolver in a fragment
        Cursor cursor = requireActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        while (true) {
            assert cursor != null;
            if (!cursor.moveToNext()) break;
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists()) {
                songsList.add(songData);
            }
        }

        if (songsList.size() == 0) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            MusicListAdapter musicListAdapter = new MusicListAdapter(songsList, requireContext());
            musicListAdapter.setOnItemClickListener(this);
            recyclerView.setAdapter(musicListAdapter);
        }

        originalSongsList = new ArrayList<>(songsList);

        // Set up the SearchView
        SearchView searchView = root.findViewById(R.id.searchbar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return true;
            }
        });

        return root;
    }

    @Override
    public void onItemClick(int position) {
        // Handle item click, start the MusicPlayerActivity with the selected song
        Intent intent = new Intent(requireContext(), MusicPlayerActivity.class);
        intent.putExtra("LIST", songsList);
        MyMediaPlayer.currentIndex = position;
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView != null) {
            recyclerView.setAdapter(new MusicListAdapter(songsList, requireContext()));
        }
    }

    boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) requireContext(), Manifest.permission.READ_MEDIA_AUDIO)) {
            Toast.makeText(requireContext(), "READ PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS", Toast.LENGTH_LONG).show();
        } else
            ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 123);
    }



    private void refreshSongsList() {
        // Clear the current songs list
        songsList.clear();

        // Fetch the latest songs from the device
        fetchLatestSongs();

        // Notify the adapter that the data has changed
        recyclerView.getAdapter().notifyDataSetChanged();
    }
    private void fetchLatestSongs() {
        // Fetch the latest songs from the device
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " !=0";

        Cursor cursor = requireActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while (true) {
            assert cursor != null;
            if (!cursor.moveToNext()) break;
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists()) {
                songsList.add(songData);
            }
        }
    }
    private void filterSongs(String query) {
        // Clear the current songs list
        songsList.clear();

        // If the query is empty, restore the original list
        if (query.isEmpty()) {
            songsList.addAll(originalSongsList);
        } else {
            // Filter the songs based on the query
            for (AudioModel song : originalSongsList) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    songsList.add(song);
                }
            }
        }

        // Notify the adapter that the data has changed
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // ... other methods ...
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
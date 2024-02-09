package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.home.AudioModel;
import com.example.myapplication.ui.home.MusicPlayerActivity;
import com.example.myapplication.ui.home.MyMediaPlayer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    private static final int RC_SIGN_IN = 123;
    private Drive driveService;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private ActivityMainBinding binding;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    private String displayName;
    private String email;
    private String photoUrl;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        // Handle sign-in failure or user cancellation
                        Log.w(TAG, "Sign-in failed. Result code: " + resultCode);

                        // Display a message to the user
                        Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }

        );

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setVisibility(View.INVISIBLE);
        binding.appBarMain.fab.setOnClickListener(view -> openMusicPlayerActivity());

        mediaPlayer.setOnPreparedListener(mp -> showFab());

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        MenuItem signInMenuItem = navigationView.getMenu().findItem(R.id.signIn);
        signInMenuItem.setOnMenuItemClickListener(menuItem -> {
            signIn();
            return true;
        });
        MenuItem logoutMenuItem = navigationView.getMenu().findItem(R.id.logout);
        logoutMenuItem.setOnMenuItemClickListener(menuItem -> {
            logout();
            return true;
        });
        MenuItem refreshDriveSongs = navigationView.getMenu().findItem(R.id.refresh);
        refreshDriveSongs.setOnMenuItemClickListener(MenuItem ->{
           refreshSongsList();
           return true;
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gdrive)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        fetchLatestSongs();

    }

    private void logout() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut()
                    .addOnCompleteListener(this, task -> {
                        // Handle sign out result
                        if (task.isSuccessful()) {
                            // Revoke access if sign out was successful
                            revokeAccess();
                        } else {
                            // Handle sign out failure
                            Toast.makeText(this, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void revokeAccess() {
        if (googleSignInClient != null) {
            googleSignInClient.revokeAccess()
                    .addOnCompleteListener(this, task -> {
                        // Handle access revocation result
                        if (task.isSuccessful()) {
                            updateUIAfterRevoke();
                        } else {
                            // Access revocation failed
                            Toast.makeText(this, "Access revocation failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }




    private void updateUIAfterRevoke() {
        // Assuming binding is a class-level variable
        if (binding != null && binding.navView != null) {
            View headerView = binding.navView.getHeaderView(0);
            TextView userNameTextView = headerView.findViewById(R.id.user_name);
            TextView emailTextView = headerView.findViewById(R.id.user_email);
            ImageView userImage = headerView.findViewById(R.id.user_image);

            // Set default values for username, email, and user image after access revocation
            userNameTextView.setText(getString(R.string.user_name));
            emailTextView.setText(getString(R.string.user_email));
            userImage.setImageResource(R.drawable.user);
        }
    }


    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    public Drive getDriveService() {
        return driveService;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void initializeDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        // Initialize the Drive service with the GoogleAccountCredential
        driveService = new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                credential)
                .setApplicationName("MyMusic")
                .build();

    }
    private void fetchLatestSongs() {
        // Fetch the latest songs from the device
        if (driveService != null) {
            try {
                FileList driveFiles = driveService.files().list().execute();
                List<AudioModel> driveAudioModels = convertDriveFilesToAudioModel(driveFiles.getFiles());

                // Add the Drive songs to the songsList
                songsList.addAll(driveAudioModels);

                // Notify the adapter that the data has changed
                recyclerView.getAdapter().notifyDataSetChanged();

                // Display the number of songs fetched in a Toast message
                int numberOfSongsFetched = driveAudioModels.size();
                String toastMessage = numberOfSongsFetched + " songs fetched.";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error fetching songs. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Existing methods and code...

    private List<AudioModel> convertDriveFilesToAudioModel(List<com.google.api.services.drive.model.File> driveFiles) {
        List<AudioModel> audioModels = new ArrayList<>();
        for (com.google.api.services.drive.model.File driveFile : driveFiles) {
            // Convert each Drive file to your AudioModel class
            AudioModel audioModel = new AudioModel(driveFile.getId(), driveFile.getName(), null); // Add necessary attributes
            audioModels.add(audioModel);
        }
        return audioModels;
    }


    public String getSignedInUsername() {
        return displayName;
    }

    public String getSignedInEmail() {
        return email;
    }

    public String getSignedInPhoto() {
        return photoUrl;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            // User is signed in, you can perform actions here

            // Example: Get user details
            displayName = account.getDisplayName();
            email = account.getEmail();
            photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

            // Update username and email in the header
            View headerView = binding.navView.getHeaderView(0);
            TextView userNameTextView = headerView.findViewById(R.id.user_name);
            TextView emailTextView = headerView.findViewById(R.id.user_email);
            ImageView userImage= headerView.findViewById(R.id.user_image);

            userNameTextView.setText(displayName);
            emailTextView.setText(email);
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.user) // Add a placeholder drawable if needed
                    .error(R.drawable.user) // Add an error drawable if loading fails
                    .into(userImage);

            // Example: Display user details in a toast
            String userInfo = "Welcome, " + displayName + "\nEmail: " + email + "\nPhoto URL: " + photoUrl;
            Toast.makeText(this, userInfo, Toast.LENGTH_LONG).show();

//            // Example: Open another activity (replace YourNextActivity.class with the actual class)
//            Intent intent = new Intent(this, YourNextActivity.class);
//            startActivity(intent);

            // Add any other actions you want to perform after successful sign-in
        } else {
            // User is signed out, update UI accordingly (if needed)
        }
    }


    private void openMusicPlayerActivity() {
        // Music is playing, open MusicPlayerActivity
        Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);

        // Pass the current 'songsList' to MusicPlayerActivity
        intent.putExtra("LIST", songsList);

        startActivity(intent);
    }

    private void showFab() {
        // Make the FAB visible
        binding.appBarMain.fab.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void refreshSongsList() {
        if (driveService != null) {
            try {
                FileList driveFiles = driveService.files().list().execute();
                List<AudioModel> driveAudioModels = convertDriveFilesToAudioModel(driveFiles.getFiles());

                // Clear the current songs list
                songsList.clear();

                // Add the Drive songs to the songsList
                songsList.addAll(driveAudioModels);

                // Notify the adapter that the data has changed
                recyclerView.getAdapter().notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error refreshing songs list. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

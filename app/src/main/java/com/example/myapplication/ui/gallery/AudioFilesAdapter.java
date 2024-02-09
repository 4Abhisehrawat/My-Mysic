package com.example.myapplication.ui.gallery;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.gallery.AudioFile;

import java.util.List;

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.ViewHolder> {
    private List<AudioFile> audioFiles;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AudioFile audioFile);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAudioFiles(List<AudioFile> audioFiles) {
        this.audioFiles = audioFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioFile audioFile = audioFiles.get(position);
        holder.fileNameTextView.setText(audioFile.getFileName());

        // Set a click listener on the item view
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(audioFile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioFiles != null ? audioFiles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.AudioFile);
            // Initialize other views if needed
        }
    }
}

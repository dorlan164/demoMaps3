package com.example.demomaps3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.chrisbanes.photoview.PhotoView;

public class FullScreenImagesAdapter
        extends RecyclerView.Adapter<FullScreenImagesAdapter.VH> {

    private final int[] images;

    public FullScreenImagesAdapter(int[] images) {
        this.images = images;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fullscreen_image, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.photoView.setImageResource(images[position]);
        // PhotoView ya permite pellizcar para zoom automáticamente
    }

    @Override public int getItemCount() {
        return images.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        PhotoView photoView;
        VH(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }
}

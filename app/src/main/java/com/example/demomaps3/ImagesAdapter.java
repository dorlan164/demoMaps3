import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class ImagesAdapter
        extends RecyclerView.Adapter<ImagesAdapter.VH> {

    private final int[] images;
    public ImagesAdapter(int[] images) {
        this.images = images;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new VH(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // carga con Glide para ajustado y caché
        Glide.with(holder.imageView.getContext())
                .load(images[position])
                .into(holder.imageView);
    }

    @Override public int getItemCount() {
        return images.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView imageView;
        VH(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}

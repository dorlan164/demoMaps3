package com.example.demomaps3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;               // ← import correcto de AndroidX
import androidx.viewpager2.widget.ViewPager2;


public class FullscreenCarouselFragment extends Fragment {
    private static final String ARG_IMAGES = "images";
    private static final String ARG_START_POS = "position";

    public static FullscreenCarouselFragment newInstance(int[] images, int startPos) {
        Bundle args = new Bundle();
        args.putIntArray(ARG_IMAGES, images);
        args.putInt(ARG_START_POS, startPos);
        FullscreenCarouselFragment f = new FullscreenCarouselFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle saved) {
        return inf.inflate(R.layout.fragment_fullscreen_carousel, parent, false);
    }

    @Override
    public void onViewCreated(View v, Bundle saved) {
        int[] images = requireArguments().getIntArray(ARG_IMAGES);
        int startPos = requireArguments().getInt(ARG_START_POS, 0);

        ViewPager2 vp = v.findViewById(R.id.viewPagerFullScreen);
        FullScreenImagesAdapter adapter = new FullScreenImagesAdapter(images);
        vp.setAdapter(adapter);
        vp.setCurrentItem(startPos, false);
    }
}
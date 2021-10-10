package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.make.dots.dotsindicator.DotsIndicator;
import com.otaliastudios.zoom.ZoomImageView;

import java.util.List;

public class ImageViewPager extends DialogFragment {
    private View v;


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.image_view_page_background));
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.image_view_pager, container, false);

        return v;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager viewPager = v.findViewById(R.id.image_view_pager);
        DotsIndicator dotsIndicator = v.findViewById(R.id.dotsIndicator_image_view_pager);
        ImageButton closeButton = v.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> ImageViewPager.this.dismiss());
        Bundle bundle = getArguments();
        if (bundle != null) {
            List<String> imageUrls = bundle.getStringArrayList(Config.IMAGE_URL);
            ViewPagerAdapterImageView viewPagerAdapterImageView = new ViewPagerAdapterImageView(getActivity(), imageUrls);
            viewPager.setAdapter(viewPagerAdapterImageView);
            dotsIndicator.setViewPager(viewPager);
            viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
        }


    }

}


class ViewPagerAdapterImageView extends PagerAdapter {
    Context context;
    private final List<String> imageUrls;

    ViewPagerAdapterImageView(Context context , List<String> imageUrls){
        this.context = context;
        this.imageUrls = imageUrls;

    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ZoomImageView imageView = new ZoomImageView(context);
        imageView.setTransformation(ZoomImageView.TRANSFORMATION_CENTER_INSIDE, ZoomImageView.TRANSFORMATION_GRAVITY_AUTO);
        imageView.setZoomEnabled(true);
        imageView.setOverPinchable(true);
        imageView.setHorizontalPanEnabled(true);
        imageView.setVerticalPanEnabled(true);
//        imageView.setOneFingerScrollEnabled(true);
//        imageView.setTwoFingersScrollEnabled(true);
//        imageView.setThreeFingersScrollEnabled(true);
        imageView.setMinZoom(0.7f);
        imageView.setMaxZoom(2.5f);
        imageView.setAnimationDuration(280);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imageUrls.get(position));
        // Load the image using Glide
        imageView.setPadding(3, 3, 3, 3);
        GlideApp.with(this.context)
                .load(storageReference)
                .into(imageView);
        ViewPager vp = (ViewPager) container;
        vp.addView(imageView);
        return imageView;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

}

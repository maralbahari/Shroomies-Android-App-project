package com.example.shroomies;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.maps.model.Circle;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    private List<Uri> imageUris;
    ViewPagerAdapter(Context context , List<Uri> imageUris){
        this.context = context;
        this.imageUris = imageUris;

    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setPadding(5,5,5,5);
//        Transformation transformation = new context.getDrawable(R.drawable.post_card_rectangle_round));

        Picasso.get().load(imageUris.get(position))
                .fit()
                .centerCrop()
                .transform(
                        new RoundedCornersTransformation(50 ,0)
                )
                .into(imageView);
        container.addView(imageView);

        return imageView;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

}

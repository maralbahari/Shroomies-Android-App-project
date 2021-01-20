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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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


        MultiTransformation multiLeft = new MultiTransformation(new CenterCrop(), new RoundedCorners(25));

//        Glide.with(context)
//                .load(message.imageUrl)
//                .apply(bitmapTransform(multiLeft))
//                .into(aq.id(R.id.ivSingleImage).getImageView());

        Glide.with(context)
                .load(imageUris.get(position))
                .transform(multiLeft)
                .into(imageView);
        container.addView(imageView);

        return imageView;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

}

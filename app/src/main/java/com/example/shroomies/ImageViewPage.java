package com.example.shroomies;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.snackbar.Snackbar;
import com.otaliastudios.zoom.ZoomImageView;

public class ImageViewPage extends AppCompatActivity {
    private ImageButton closeImageButton;
    private ZoomImageView imageView;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_page);
        closeImageButton = findViewById(R.id.close_image_view);
        imageView = findViewById(R.id.image_view_image_view_page);
        relativeLayout = findViewById(R.id.root_view_image_view_page);

        closeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewPage.this.finish();
            }
        });
        Bundle extras = getIntent().getExtras();
        String image = extras.getString("imagebitmap");

        if(image !=null){
            GlideApp.with(getApplication())
                    .load(image)
                    .dontAnimate()
                    .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                    .into(imageView);

        }else {
            imageView.setImageDrawable(getApplication().getDrawable(R.drawable.ic_no_file_added));
            Snackbar.make(relativeLayout , "Couldn't load the image" , Snackbar.LENGTH_LONG).show();
        }

    }


}

package com.example.shroomies;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.MapView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.make.dots.dotsindicator.DotsIndicator;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ApartmentViewPage extends AppCompatActivity  {
    ViewPager viewPager;
    Button messageButton;
    DotsIndicator dotsIndicator;
    TextView priceTextView;
    TextView descriptionTextView;
    ViewPagerAdapterApartmentView viewPagerAdapterApartmentView;
    Apartment apartment;
    TextView numberOfRoomMates;
    TextView date;
    TextView username;
    MapView mapView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_page);
        if(getIntent().getExtras()!=null){
            apartment= new Apartment();
            apartment = getIntent().getExtras().getParcelable("apartment");
        }

        viewPager = findViewById(R.id.view_pager_apartment_view);
        dotsIndicator = findViewById(R.id.dotsIndicator_apartment_view);
        priceTextView = findViewById(R.id.price_text_view);
        descriptionTextView = findViewById(R.id.user_description_text_view);
        numberOfRoomMates = findViewById(R.id.number_of_roommates_text_view);
        date = findViewById(R.id.date_of_post_text_view);
        username = findViewById(R.id.name_of_user_text_view);
        




        //set the desicription
        descriptionTextView.setText(apartment.getDescription());
        //set the no of roommates
        numberOfRoomMates.setText(Integer.toString(apartment.getNumberOfRoommates()));
        // set the date
        date.setText(apartment.getDate());
        // get the username and profile picture from the database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(apartment.getUserID());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User();
                user = snapshot.getValue(User.class);
                username.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        viewPagerAdapterApartmentView = new ViewPagerAdapterApartmentView(getApplicationContext() ,apartment.getImage_url());
        viewPager.setAdapter(viewPagerAdapterApartmentView);
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());



    }

}

// create a new view pager adapter
class ViewPagerAdapterApartmentView extends PagerAdapter {
    Context context;
    private List<String> imageUrls;
    ViewPagerAdapterApartmentView(Context context , List<String> imageUrls){
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
        ImageView imageView = new ImageView(context);


        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imageUrls.get(position));
        Toast.makeText(context, imageUrls.get(position),Toast.LENGTH_LONG).show();
        // Load the image using Glide
        GlideApp.with(this.context)
                .load(storageReference)
                .centerCrop()
                .centerInside()
                .fitCenter()
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
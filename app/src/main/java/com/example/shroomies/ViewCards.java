package com.example.shroomies;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.widget.SocialTextView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ViewCards extends DialogFragment {
    //views
    private View v;
    private ImageButton close , expandImageButton;
    private TextView title,dueDate,description, expensesTextView;
    private ImageView attachedFile;
    private SocialTextView mention;
    private View importanceView;
    private RecyclerView viewCardRecycler;
    private NestedScrollView nestedScrollView;
    private RelativeLayout relativeLayout;
    private LottieAnimationView lottieAnimationView;
    //data
    private TasksCard tasksCard;
    private ExpensesCard expensesCard;
    private Bundle bundle;
    private UserAdapterSplitExpenses splitAdapter;
    private ArrayList<User> shroomiesList;
    //firebase
    private DatabaseReference rootRef;
    //variables
    private boolean taskCardSelected;
    private String imagePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_view_cards, container, false);

        rootRef= FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.expense_card_image_background);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title=v.findViewById(R.id.view_card_title_tv);
        dueDate=v.findViewById(R.id.view_card_date);
        description=v.findViewById(R.id.view_card_description);
        mention=v.findViewById(R.id.view_card_mention);
        attachedFile=v.findViewById(R.id.view_card_image);
        importanceView=v.findViewById(R.id.view_card_importance);
        close=v.findViewById(R.id.close_view_card);
        viewCardRecycler=v.findViewById(R.id.view_card_recycler);
        expensesTextView = v.findViewById(R.id.expenses_text_view);
        expandImageButton = v.findViewById(R.id.expand_image_card_view);
        relativeLayout = v.findViewById(R.id.view_card_relative_layout);
        nestedScrollView = v.findViewById(R.id.view_card_nested_scroll_view);
        lottieAnimationView = v.findViewById(R.id.lottie_downloading_animation);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        viewCardRecycler.setLayoutManager(linearLayoutManager);

        relativeLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        nestedScrollView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);


        bundle=this.getArguments();
        if(bundle!=null){
            taskCardSelected=bundle.getBoolean("FROM_TASK_TAB");
            if(taskCardSelected){
                tasksCard=bundle.getParcelable("CARD_DETAILS");
                viewCardRecycler.setVisibility(View.GONE);
                attachedFile.setVisibility(View.GONE);
                title.setText(tasksCard.getTitle());
                String due= tasksCard.getDueDate();
                String descriptionCard=tasksCard.getDescription();
                String mentions=tasksCard.getMention();
                String importance=tasksCard.getImportance();
                if(!due.isEmpty() ){
                    dueDate.setText(due);
                }if(!descriptionCard.isEmpty()){
                    description.setText(descriptionCard);
                }if(!mentions.isEmpty()){
                    mention.setText(mentions);
                }
                if(!importance.isEmpty() ){
                    switch (importance) {
                        case  "2":
                            importanceView.setBackgroundColor(getActivity().getColor(R.color.canceRed));
                            break;
                        case  "1":
                            importanceView.setBackgroundColor(getActivity().getColor(R.color.orange));
                            break;
                        default:
                            importanceView.setBackgroundColor(getActivity().getColor(R.color.okGreen));
                    }

                }
            }else{
                expensesCard=bundle.getParcelable("CARD_DETAILS");
                title.setText(expensesCard.getTitle());
                String due= expensesCard.getDueDate();
                String descriptionCard=expensesCard.getDescription();
                String mentions=expensesCard.getMention();
                String importance=expensesCard.getImportance();
                String fileType = expensesCard.getFileType();
                imagePath=expensesCard.getAttachedFile();
                HashMap<String, Float> membersShares=expensesCard.getMembersShares();
                if(!membersShares.isEmpty()){
                    getMembersShares(membersShares);
                }else {
                    expensesTextView.setVisibility(View.GONE);
                }
                if(!due.isEmpty() ){
                    dueDate.setText(due);
                }if(!descriptionCard.isEmpty()){
                    description.setText(descriptionCard);
                }if(!mentions.isEmpty()){
                    mention.setMentionColor(Color.BLUE);
                    mention.setText(mentions);
                }

                        if(!importance.isEmpty() ){
                            switch (importance) {
                                case "0":
                                    importanceView.setBackgroundColor(getActivity().getColor(R.color.okGreen));
                                    break;
                                case  "2":
                                    importanceView.setBackgroundColor(getActivity().getColor(R.color.canceRed));
                                    break;
                                case  "1":
                                    importanceView.setBackgroundColor(getActivity().getColor(R.color.orange));
                                    break;
                                default:
                                    importanceView.setBackgroundColor(Color.parseColor("#F5CB5C"));
                            }
                    }

                if(!imagePath.isEmpty()){
                    if(!fileType.equals("pdf")) {
                        GlideApp.with(getContext())
                                .load(imagePath)
                                .dontAnimate()
                                .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                                .error(R.drawable.ic_no_file_added)
                                .into(attachedFile);
                    }else{
                        expandImageButton.setImageDrawable(getActivity().getDrawable(R.drawable.ic_download));
                        attachedFile.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pdf_icon));
                    }
                }else{
                    attachedFile.setImageDrawable(getActivity().getDrawable(R.drawable.ic_no_file_added));

                }

            }
        }
        expandImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagePath!=null){
                    if(expensesCard.getFileType().equals("pdf")){
                        Dexter.withContext(getActivity())
                                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .withListener(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                            // navigate user to app settings
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }else {
                                            //start the animation in place of the icon
                                            expandImageButton.setVisibility(View.GONE);
                                            lottieAnimationView.setVisibility(View.VISIBLE);
                                            download(imagePath);
                                        }
                                    }
                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                        permissionToken.continuePermissionRequest();

                                    }
                                }).check();
                    }else {
                        Intent intent = new Intent(getActivity(), ImageViewPage.class);
                        Bundle extras = new Bundle();
                        extras.putString("imagebitmap", imagePath);
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                }

            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }

    private void getMembersShares(final HashMap<String, Float> membersShares) {
        shroomiesList=new ArrayList<>();
        splitAdapter= new UserAdapterSplitExpenses(shroomiesList,getContext(),true);
        viewCardRecycler.setAdapter(splitAdapter);


        for (final String id: membersShares.keySet()){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        user.setSharedAmount(Float.valueOf(membersShares.get(id).toString()));
                        shroomiesList.add(user);
                        splitAdapter.notifyItemInserted(shroomiesList.indexOf(user));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    public void download(String imagePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(imagePath);
        final File rootPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Shroomies downloads");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddhhmmssMs");
        String datetime = simpleDateFormat.format(date);
        final File localFile = new File(rootPath,datetime);

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                expandImageButton.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.GONE);

                android.util.Log.e("firebase ", "local item file created" + localFile.toString());
                if (localFile.canRead()){
                }
                //if download is successful
                //open the file
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri pdfUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", localFile);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intent1 = Intent.createChooser(intent, "Open With");
                try {
                    startActivity(intent1);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                    Toast.makeText(getActivity(), "Couldn't find PDF reader", Toast.LENGTH_LONG).show();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                expandImageButton.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.GONE);
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                Toast.makeText(getActivity(), "Failed to download file", Toast.LENGTH_LONG).show();
            }
        });
    }
}
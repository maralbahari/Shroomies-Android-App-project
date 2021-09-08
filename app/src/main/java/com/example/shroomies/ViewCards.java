package com.example.shroomies;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class ViewCards extends DialogFragment {
    //views
    private View v;
    private ImageButton close , expandImageButton;
    private TextView title,dueDate,description, expensesTextView , noMentionsAdded;
    private ImageView attachedFile;
    private ChipGroup mentionChipGroup;
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
    //variables
    private boolean taskCardSelected;
    private String imagePath;
    private HashMap<String , User>  membersHashMap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_view_cards, container, false);

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
        mentionChipGroup=v.findViewById(R.id.view_card_mention);
        attachedFile=v.findViewById(R.id.view_card_image);
        importanceView=v.findViewById(R.id.view_card_importance);
        close=v.findViewById(R.id.close_view_card);
        viewCardRecycler=v.findViewById(R.id.view_card_recycler);
        expensesTextView = v.findViewById(R.id.expenses_text_view);
        expandImageButton = v.findViewById(R.id.expand_image_card_view);
        relativeLayout = v.findViewById(R.id.view_card_relative_layout);
        nestedScrollView = v.findViewById(R.id.view_card_nested_scroll_view);
        lottieAnimationView = v.findViewById(R.id.lottie_downloading_animation);
        noMentionsAdded = v.findViewById(R.id.no_mentions_added);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        viewCardRecycler.setLayoutManager(linearLayoutManager);

        relativeLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        nestedScrollView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);


        bundle=this.getArguments();
        if(bundle!=null){
            membersHashMap = (HashMap<String, User>) bundle.getSerializable(Config.members);
            taskCardSelected=bundle.getBoolean("FROM_TASK_TAB");
            if(taskCardSelected){
                tasksCard=bundle.getParcelable("CARD_DETAILS");
                setUpTaskData(tasksCard);
            }else{
                expensesCard=bundle.getParcelable("CARD_DETAILS");
                setUpExpensesData(expensesCard);

            }
        }
        expandImageButton.setOnClickListener(v -> {
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

        });
        close.setOnClickListener(view1 -> dismiss());


    }

    private void setUpExpensesData(ExpensesCard expensesCard) {
        title.setText(expensesCard.getTitle());
        String due= expensesCard.getDueDate();
        String descriptionCard=expensesCard.getDescription();
        HashMap<String , String> mentions=expensesCard.getMention();
        HashMap<String, Integer> membersShares= expensesCard.getMembersShares();
        String importance=expensesCard.getImportance();
        String fileType = expensesCard.getFileType();
        imagePath=expensesCard.getAttachedFile();
        if(membersShares!=null){
            if(!membersShares.isEmpty()){
                getMembersShares(membersShares);
            }else{
                expensesTextView.setVisibility(View.GONE);
            }
        }else {
            expensesTextView.setVisibility(View.GONE);
        }

        if(due!=null ){
            DateTimeFormatter dateformat =  DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                    .withZone(TimeZone.getDefault().toZoneId());
            DateTimeFormatter displayFormat =  DateTimeFormatter.ofPattern("EEE, MMM d")
                    .withZone(TimeZone.getDefault().toZoneId());
            dueDate.setText(displayFormat.format(dateformat.parse(due)));

        }if(descriptionCard!=null){
            description.setText(descriptionCard);
        }

        if(mentions!=null){
            if(!mentions.isEmpty()){
                addMentionChips(mentions);
            }else{
                noMentionsAdded.setVisibility(View.VISIBLE);
            }

        }else{
            noMentionsAdded.setVisibility(View.VISIBLE);
        }


        if(importance!=null ){
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

        if(imagePath!=null){
            if(!imagePath.isEmpty()){
                if(!fileType.equals("pdf")) {
                    GlideApp.with(getContext())
                            .load(imagePath)
                            .dontAnimate()
                            .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                            .error(R.drawable.ic_no_file_added)
                            .into(attachedFile);
                }else{
                    expandImageButton.setBackgroundResource(R.drawable.ic_download);
                    attachedFile.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pdf_icon));
                }
            }else{
                expandImageButton.setVisibility(View.GONE);
                attachedFile.setImageDrawable(getActivity().getDrawable(R.drawable.ic_no_file_added));
            }

        }else{
            expandImageButton.setVisibility(View.GONE);
            attachedFile.setImageDrawable(getActivity().getDrawable(R.drawable.ic_no_file_added));

        }
    }




    private void setUpTaskData(TasksCard tasksCard) {
        expensesTextView.setVisibility(View.GONE);
        viewCardRecycler.setVisibility(View.GONE);
        attachedFile.setVisibility(View.GONE);
        expandImageButton.setVisibility(View.GONE);
        title.setText(tasksCard.getTitle());
        // add extra margins so it doesn't overlap with the close button
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) title.getLayoutParams();
        params.topMargin = 100; params.setMarginStart(30); params.setMarginEnd(30);
        title.setLayoutParams(params);
        String due= tasksCard.getDueDate();
        String descriptionCard=tasksCard.getDescription();
        String importance=tasksCard.getImportance();
        HashMap<String , String> mentions=tasksCard.getMention();
        if(!descriptionCard.isEmpty()){
            description.setText(descriptionCard);
        }
        if(due!=null) {
            if (!due.isEmpty()) {
                DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("EEE, MMM d")
                        .withZone(TimeZone.getDefault().toZoneId());
                dueDate.setText(displayFormat.format(dateformat.parse(due)));

            }
        }

        if(mentions!=null){
            if(!mentions.isEmpty()){
                addMentionChips(mentions);
            }else{
                noMentionsAdded.setVisibility(View.VISIBLE);
            }

        }else{
            noMentionsAdded.setVisibility(View.VISIBLE);
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
    }

    private void addMentionChips(HashMap<String,String> mentions) {
        for(Map.Entry<String,String> mention
        :mentions.entrySet()){
            if(membersHashMap.get(mention.getKey())!=null){
                Chip chip = new Chip(getActivity());
                chip.setText(membersHashMap.get(mention.getKey()).getUsername());
                mentionChipGroup.addView(chip);
            }
        }
    }

    private void getMembersShares(final HashMap<String, Integer> membersShares) {
        shroomiesList=new ArrayList<>();
        //add the  user to the list with the shared amount
        for (Map.Entry<String , Integer> share
        :membersShares.entrySet()){
            int userShare = share.getValue();
            String userID = share.getKey();
            if(membersHashMap.get(userID)!=null){
                User user  = membersHashMap.get(userID);
                user.setSharedAmount(userShare);
                shroomiesList.add(user);
            }
        }
        splitAdapter= new UserAdapterSplitExpenses(shroomiesList,getContext(),true);
        viewCardRecycler.setAdapter(splitAdapter);
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

        storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            expandImageButton.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.GONE);

            Log.e("firebase ", "local item file created" + localFile.toString());

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

        }).addOnFailureListener(exception -> {
            expandImageButton.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.GONE);
            Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            Toast.makeText(getActivity(), "Failed to download file", Toast.LENGTH_LONG).show();
        });
    }
}
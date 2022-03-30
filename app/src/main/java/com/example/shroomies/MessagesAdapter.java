package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.shroomies.localDatabase.ImagesPaths;
import com.example.shroomies.localDatabase.ImagesPathsDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    static Context context;

    public MessagesAdapter(List<Messages> userMessagesList, Context context) {
        this.userMessagesList = userMessagesList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_chat_cards, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.MessageViewHolder holder, int position) {
        String senderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        holder.senderLinearLayout.setVisibility(View.GONE);
        holder.receiverLinearLayout.setVisibility(View.GONE);
        holder.receiverImageLinearLayout.setVisibility(View.GONE);
        holder.senderImageLinearLayout.setVisibility(View.GONE);
        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(senderID)) {
                holder.senderLinearLayout.setVisibility(View.VISIBLE);
                holder.senderTextView.setText(messages.getText());
                holder.senderDate.setText(messages.getTime());
            } else {
                holder.receiverLinearLayout.setVisibility(View.VISIBLE);
                holder.receiverTextView.setText(messages.getText());
                holder.receiverDate.setText(messages.getTime());
            }
        }
        if (fromMessageType.equals("image")) {
            if (ImagesPathsDatabase.getInstance(context).imagePathsDao().isRowExists(messages.getMessageId())) {
                ImagesPaths imagesPaths = ImagesPathsDatabase.getInstance(context).imagePathsDao().getImagePath(messages.getMessageId());
                if (messages.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                    GlideApp.with(context)
                            .load(imagesPaths.getLocalPath())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .transform(new RoundedCorners(25))
                            .into(holder.senderImageView);
                }else{
                    GlideApp.with(context)
                            .load(imagesPaths.getLocalPath())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .transform(new RoundedCorners(25))
                            .into(holder.receiverImageView);
                }
            } else {
                // load the image and decrypt it
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                //TODO fix max download size bytes
                storageReference.child(messages.getText()).getBytes(1000000000).addOnSuccessListener(bytes -> {
                    downloadImage(bytes,messages);
//            MessagesAdapter.setImage(bitmap , messages , messageViewHolder , uID);
                    if (messages.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                        GlideApp.with(context)
                                .load(bytes)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .transform(new RoundedCorners(25))
                                .into(holder.senderImageView);
                    }else{
                        GlideApp.with(context)
                                .load(bytes)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .transform(new RoundedCorners(25))
                                .into(holder.receiverImageView);
                    }
                });
            }

            if (fromUserID.equals(senderID)) {
                holder.senderImageLinearLayout.setVisibility(View.VISIBLE);
                holder.senderImageDate.setText(messages.getTime());

                holder.senderImageDate.setText(messages.getTime());
            } else {
                holder.receiverImageLinearLayout.setVisibility(View.VISIBLE);

                holder.receiverImageDate.setText(messages.getTime());
            }
        }

    }

    private void downloadImage(byte[] bytes, Messages messages) {

        if (!verifyPermissions()) {
            return;
        }
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "shroomies" + "/";
        File dir = new File(dirPath);
        String fileName = ""+System.currentTimeMillis()+".jpg";
        GlideApp.with(context)
                .load(bytes)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();
                        saveImage(bitmap,dir,fileName,messages);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private Boolean verifyPermissions() {

        // This will return the current Status
        int permissionExternalMemory = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {

            String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            // If permission not granted then ask for permission real time.
            ActivityCompat.requestPermissions((Activity) context, STORAGE_PERMISSIONS, 1);
            return false;
        }

        return true;

    }

    private void saveImage(Bitmap bitmap, File dir, String fileName, Messages messages) {

        boolean isFolderCreated = dir.exists();
        Log.i("save image path",dir.getAbsolutePath());
        if (!dir.exists()) {
            isFolderCreated = dir.mkdir();
        }
        if (isFolderCreated) {
            File imageFile = new File(dir,fileName);
            String savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, fOut);
                fOut.close();
                ImagesPaths imagesPaths = new ImagesPaths(messages.getMessageId(),imageFile.getAbsolutePath());
                ImagesPathsDatabase.getInstance(context).imagePathsDao().insertImagePath(imagesPaths);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Error creating the folder!", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, receiverTextView, senderDate, receiverDate, receiverImageDate, senderImageDate;
        private LinearLayout receiverLinearLayout, receiverImageLinearLayout, senderImageLinearLayout;
        private LinearLayout senderLinearLayout;
        ImageView senderImageView;
        ImageView receiverImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverLinearLayout = itemView.findViewById(R.id.receiver_box_card_layout);
            senderLinearLayout = itemView.findViewById(R.id.sender_box_card_layout);
            senderTextView = itemView.findViewById(R.id.sender_box_card);
            senderDate = itemView.findViewById(R.id.sender_box_card_date);
            receiverDate = itemView.findViewById(R.id.reciever_box_card_date);
            receiverTextView = itemView.findViewById(R.id.receiver_box_card);
            senderImageView = itemView.findViewById(R.id.sender_image_view);
            receiverImageView = itemView.findViewById(R.id.receiver_image_view);
            receiverImageLinearLayout = itemView.findViewById(R.id.receiver_image_view_linear_layout);
            senderImageLinearLayout = itemView.findViewById(R.id.sender_image_view_linear_layout);
            receiverImageDate = itemView.findViewById(R.id.reciever_box_card_image_date);
            senderImageDate = itemView.findViewById(R.id.sender_box_card_date);

        }

    }
}

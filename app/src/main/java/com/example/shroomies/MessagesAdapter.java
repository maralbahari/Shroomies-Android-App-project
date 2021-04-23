package com.example.shroomies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.virgilsecurity.android.ethree.interaction.EThree;
import com.virgilsecurity.common.model.Data;
import com.virgilsecurity.sdk.cards.Card;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    static Context context;
    private EThree ethree;
    private Card recipientVirgilCard;
    private Card senderVirgilCard;

    public MessagesAdapter(List<Messages> userMessagesList, Context context, Card recipientVirgilCard, Card senderVirgilCard) {
        this.userMessagesList = userMessagesList;
        this.context = context;
        this.ethree = LoginActivity.eThree;
        this.recipientVirgilCard = recipientVirgilCard;
        this.senderVirgilCard = senderVirgilCard;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_chat_cards, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
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
            // load the image and decrypt it
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            //TODO fix max download size bytes
            storageReference.child(messages.getText()).getBytes(1000000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    DecodeImageAsyncTask decodeImageAsyncTask = new DecodeImageAsyncTask(ethree , senderVirgilCard  , recipientVirgilCard ,holder , messages  ,mAuth.getCurrentUser().getUid() , bytes);
                    decodeImageAsyncTask.execute();


                }
            });

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
    static void setImage(Bitmap bitmap , Messages messages , MessageViewHolder messageViewHolder , String uID){
        if (messages.getFrom().equals(uID)) {
            messageViewHolder.senderImageView.setImageDrawable(new BitmapDrawable(context.getResources() , bitmap));
        }else{
            messageViewHolder.receiverImageView.setImageDrawable(new BitmapDrawable(context.getResources() , bitmap));
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

    class DecodeImageAsyncTask extends AsyncTask<String , String ,Bitmap > {
        private EThree ethree;
        private Card senderCard;
        private Card recieverCard;
        private MessagesAdapter.MessageViewHolder messageViewHolder;
        private Messages messages;
        private String uID;
        private byte[] encryptedData;

        public DecodeImageAsyncTask(EThree ethree, Card senderCard,Card recieverCard, MessagesAdapter.MessageViewHolder messageViewHolder, Messages messages ,String uID ,byte[] encryptedData) {
            this.ethree = ethree;
            this.senderCard = senderCard;
            this.messageViewHolder = messageViewHolder;
            this.messages = messages;
            this.recieverCard = recieverCard;
            this.uID = uID;
            this.encryptedData = encryptedData;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            ByteArrayOutputStream decryptedOutputStream = new ByteArrayOutputStream();

            byte[] p2pDecryptedStreamKeyData;
            // convert the string to a byte array
            byte[] decodedStreamKeyData = Base64.getDecoder().decode(messages.getStreamKeyData());

            ByteArrayInputStream encryptedInputStream = new ByteArrayInputStream(encryptedData);

            if (messages.getFrom().equals(uID)) {
                p2pDecryptedStreamKeyData = ethree.authDecrypt(new Data(decodedStreamKeyData), senderCard).getValue();
            } else {
                p2pDecryptedStreamKeyData = ethree.authDecrypt(new Data(decodedStreamKeyData), recieverCard).getValue();
            }

            ethree.decryptShared(encryptedInputStream, decryptedOutputStream, p2pDecryptedStreamKeyData, senderCard);

            byte[] decryptedImage  = decryptedOutputStream.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(decryptedImage , 0, decryptedImage.length);

          return  bitmap;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            MessagesAdapter.setImage(bitmap , messages , messageViewHolder , uID);

        }
    }


package com.org.smallcircle.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.org.smallcircle.R;
import com.org.smallcircle.activity.ChatActivity;
import com.org.smallcircle.databinding.RowChatsBinding;
import com.org.smallcircle.model.ModelChats;
import com.org.smallcircle.utils.FilterChats;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.HolderChats> implements Filterable {

    private static final String TAG = "ADAPTER_CHATS_TAG";

    private RowChatsBinding binding;

    private Context context;

    public ArrayList<ModelChats> chatsArrayList;

    private ArrayList<ModelChats> filterList;

    private FilterChats filter;

    private FirebaseAuth firebaseAuth;

    private String myUid;

    public AdapterChats(Context context, ArrayList<ModelChats> chatsArrayList) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.filterList = chatsArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderChats(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {
        ModelChats modelChats = chatsArrayList.get(position);

        loadLastMessage(modelChats, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiptUid = modelChats.getReceiptUid();

                if (receiptUid != null) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("receiptUid", receiptUid);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void loadLastMessage(ModelChats modelChats, HolderChats holder) {
        String chatKey = modelChats.getChatKey();

        Log.d(TAG, "loadLastMessage: chatKey: " + chatKey);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatKey).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String fromUid = "" + ds.child("fromUid").getValue();
                            String message = "" + ds.child("message").getValue();
                            String messageId = "" + ds.child("messageId").getValue();
                            String messageType = "" + ds.child("messageType").getValue();
                            long timestamp = (Long) ds.child("timestamp").getValue();
                            String toUid = "" + ds.child("toUid").getValue();

                            String formattedDate = Utils.formatTimestampDateTime(timestamp);

                            modelChats.setMessage(message);
                            modelChats.setMessageId(messageId);
                            modelChats.setMessageType(messageType);
                            modelChats.setFromUid(fromUid);
                            modelChats.setTimestamp(timestamp);
                            modelChats.setToUid(toUid);

                            holder.tvTime.setText(formattedDate);

                            if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)) {
                                holder.tvLastMessage.setText(message);
                            } else {
                                holder.tvLastMessage.setText("Sends attachment");
                            }
                        }

                        loadReceiptUserInfo(modelChats, holder);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadReceiptUserInfo(ModelChats modelChats, HolderChats holder) {
        String fromUid = modelChats.getFromUid();
        String toUid = modelChats.getToUid();

        String receiptUid;

        if (fromUid.equals(myUid)) {
            receiptUid = toUid;
        } else {
            receiptUid = fromUid;
        }

        modelChats.setReceiptUid(receiptUid);

        Log.d(TAG, "loadReceiptUserInfo: fromUid: " + fromUid);
        Log.d(TAG, "loadReceiptUserInfo: toUid: " + toUid);
        Log.d(TAG, "loadReceiptUserInfo: receiptUid: " + receiptUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        modelChats.setName(name);
                        modelChats.setProfileImageUrl(profileImageUrl);

                        holder.tvName.setText(name);

                        try {
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_black)
                                    .into(holder.ivProfile);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterChats(this, filterList);
        }

        return filter;
    }

    class HolderChats extends RecyclerView.ViewHolder {

        RoundedImageView ivProfile;
        TextView tvName, tvLastMessage, tvTime;

        public HolderChats(@NonNull View itemView) {
            super(itemView);

            ivProfile = binding.ivProfile;
            tvName = binding.tvName;
            tvLastMessage = binding.tvLastMessage;
            tvTime = binding.tvTime;

        }
    }
}

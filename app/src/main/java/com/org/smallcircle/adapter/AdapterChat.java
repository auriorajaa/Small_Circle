package com.org.smallcircle.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.org.smallcircle.R;
import com.org.smallcircle.model.ModelChat;
import com.org.smallcircle.utils.Utils;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChat>{

    private static final String TAG = "ADAPTER_CHAT_TAG";

    private Context context;
    private ArrayList<ModelChat> chatArrayList;

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private FirebaseUser firebaseUser;

    public AdapterChat(Context context, ArrayList<ModelChat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public HolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new HolderChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new HolderChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChat holder, int position) {
        ModelChat modelChat = chatArrayList.get(position);

        String message = modelChat.getMessage();
        String messageType = modelChat.getMessageType();
        long timestamp = modelChat.getTimestamp();

        String formattedDateTime = Utils.formatTimestampDateTime(timestamp);

        if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)) {
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.imgMessage.setVisibility(View.GONE);

            holder.txtMessage.setText(message);
        } else {
            holder.txtMessage.setVisibility(View.GONE);
            holder.imgMessage.setVisibility(View.VISIBLE);

            try {
                Glide.with(context)
                        .load(message)
                        .placeholder(R.drawable.ic_image_gray)
                        .error(R.drawable.ic_broken_image_gray)
                        .into(holder.imgMessage);
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: ", e);
            }
        }

        holder.txtTime.setText(formattedDateTime);
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatArrayList.get(position).getFromUid().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderChat extends RecyclerView.ViewHolder{

        TextView txtMessage, txtTime;
        ImageView imgMessage;

        public HolderChat(@NonNull View itemView) {
            super(itemView);

            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgMessage = itemView.findViewById(R.id.imgMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}

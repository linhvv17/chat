package com.thanguit.tuichat.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.thanguit.tuichat.R;
import com.thanguit.tuichat.databinding.ItemChatMessageReceiveBinding;
import com.thanguit.tuichat.databinding.ItemChatMessageSendBinding;
import com.thanguit.tuichat.models.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ChatMessageAdapter";

    private FirebaseAuth firebaseAuth;

    private Context context;
    private List<ChatMessage> chatMessageList;
    private String uid;
    private String avatar;
    private String senderRoom;
    private String receiverRoom;

    private final int ITEM_SEND = 1;
    private final int ITEM_RECEIVE = 2;

    private FirebaseDatabase firebaseDatabase;

    int[] emoticon = new int[]{
            R.drawable.ic_love,
            R.drawable.ic_laughing,
            R.drawable.ic_crying,
            R.drawable.ic_angry,
            R.drawable.ic_surprised,
            R.drawable.ic_cool
    };

    public ChatMessageAdapter(Context context, List<ChatMessage> chatMessageList) {
        this.context = context;
        this.chatMessageList = chatMessageList;
    }

    public ChatMessageAdapter(Context context, List<ChatMessage> chatMessageList, String uid, String avatar, String senderRoom, String receiverRoom) {
        this.context = context;
        this.chatMessageList = chatMessageList;
        this.uid = uid;
        this.avatar = avatar;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_send, parent, false);
            return new SendViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_receive, parent, false);
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = chatMessageList.get(position);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getUid().trim().equals(chatMessage.getSenderID().trim())) {
                return ITEM_SEND;
            } else {
                return ITEM_RECEIVE;
            }
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            if (uid.trim().equals(currentUser.getUid().trim())) {
                SendViewHolder sendViewHolder = (SendViewHolder) holder;
                sendViewHolder.itemChatMessageSendBinding.tvSend.setText(chatMessageList.get(holder.getLayoutPosition()).getMessage().trim());
                sendViewHolder.itemChatMessageSendBinding.tvTime.setText(chatMessageList.get(holder.getLayoutPosition()).getTime().trim());

                sendViewHolder.itemChatMessageSendBinding.llChatSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (sendViewHolder.itemChatMessageSendBinding.tvTime.getVisibility() == View.GONE) {
                            sendViewHolder.itemChatMessageSendBinding.tvTime.setVisibility(View.VISIBLE);
                        } else {
                            sendViewHolder.itemChatMessageSendBinding.tvTime.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                ReactionsConfig config = new ReactionsConfigBuilder(context)
                        .withReactions(emoticon)
                        .withPopupColor(context.getResources().getColor(R.color.color_8))
                        .build();

                if (getItemViewType(holder.getLayoutPosition()) == ITEM_SEND) {
                    SendViewHolder sendViewHolder = (SendViewHolder) holder;
                    sendViewHolder.itemChatMessageSendBinding.tvSend.setText(chatMessageList.get(holder.getLayoutPosition()).getMessage().trim());
                    sendViewHolder.itemChatMessageSendBinding.tvTime.setText(chatMessageList.get(holder.getLayoutPosition()).getTime().trim());
                    if (!chatMessageList.get(holder.getLayoutPosition()).getImage().trim().isEmpty()) {
                        sendViewHolder.itemChatMessageSendBinding.ivImage.setVisibility(View.VISIBLE);
                        sendViewHolder.itemChatMessageSendBinding.tvSend.setVisibility(View.GONE);
                        Picasso.get().load(chatMessageList.get(holder.getLayoutPosition()).getImage().trim())
                                .placeholder(R.drawable.ic_picture)
                                .error(R.drawable.ic_picture)
                                .into(sendViewHolder.itemChatMessageSendBinding.ivImage);
                    }

                    if (chatMessageList.get(holder.getLayoutPosition()).getEmoticon() >= 0) {
                        sendViewHolder.itemChatMessageSendBinding.ivEmoticon.setImageResource(emoticon[chatMessageList.get(holder.getLayoutPosition()).getEmoticon()]);
                        sendViewHolder.itemChatMessageSendBinding.ivEmoticon.setVisibility(View.VISIBLE);
                    } else {
                        sendViewHolder.itemChatMessageSendBinding.ivEmoticon.setVisibility(View.GONE);
                    }

                    sendViewHolder.itemChatMessageSendBinding.llChatSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (sendViewHolder.itemChatMessageSendBinding.tvTime.getVisibility() == View.GONE) {
                                sendViewHolder.itemChatMessageSendBinding.tvTime.setVisibility(View.VISIBLE);
                            } else {
                                sendViewHolder.itemChatMessageSendBinding.tvTime.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
                    Picasso.get().load(avatar)
                            .placeholder(R.drawable.ic_user_avatar)
                            .error(R.drawable.ic_user_avatar)
                            .into(receiveViewHolder.itemChatMessageReceiveBinding.civAvatar);
                    receiveViewHolder.itemChatMessageReceiveBinding.tvReceive.setText(chatMessageList.get(holder.getLayoutPosition()).getMessage().trim());
                    receiveViewHolder.itemChatMessageReceiveBinding.tvTime.setText(chatMessageList.get(holder.getLayoutPosition()).getTime().trim());
                    if (!chatMessageList.get(holder.getLayoutPosition()).getImage().trim().isEmpty()) {
                        receiveViewHolder.itemChatMessageReceiveBinding.ivImage.setVisibility(View.VISIBLE);
                        receiveViewHolder.itemChatMessageReceiveBinding.tvReceive.setVisibility(View.GONE);
                        Picasso.get().load(chatMessageList.get(holder.getLayoutPosition()).getImage().trim())
                                .placeholder(R.drawable.ic_picture)
                                .error(R.drawable.ic_picture)
                                .into(receiveViewHolder.itemChatMessageReceiveBinding.ivImage);
                    }

                    if (chatMessageList.get(holder.getLayoutPosition()).getEmoticon() >= 0) {
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setImageResource(emoticon[chatMessageList.get(holder.getLayoutPosition()).getEmoticon()]);
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setVisibility(View.VISIBLE);
                    } else {
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setVisibility(View.GONE);
                    }

                    receiveViewHolder.itemChatMessageReceiveBinding.llChatReceive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (receiveViewHolder.itemChatMessageReceiveBinding.tvTime.getVisibility() == View.GONE) {
                                receiveViewHolder.itemChatMessageReceiveBinding.tvTime.setVisibility(View.VISIBLE);
                            } else {
                                receiveViewHolder.itemChatMessageReceiveBinding.tvTime.setVisibility(View.GONE);
                            }
                        }
                    });

                    ReactionPopup popup = new ReactionPopup(context, config, (index) -> {
                        if (index < 0) {
                            return false;
                        }
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setImageResource(emoticon[index]);
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setVisibility(View.VISIBLE);

                        firebaseDatabase.getReference()
                                .child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(chatMessageList.get(holder.getLayoutPosition()).getMessageID())
                                .child("emoticon").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task1) {
                                firebaseDatabase.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(chatMessageList.get(holder.getLayoutPosition()).getMessageID())
                                        .child("emoticon").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task2) {
                                        String icon = index.toString().trim();
                                        String room1 = String.valueOf(task1.getResult().getValue());
                                        String room2 = String.valueOf(task2.getResult().getValue());

                                        if (!room1.isEmpty() && !room2.isEmpty()) {
                                            if (icon.equals(room1) && icon.equals(room2)) {
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(senderRoom)
                                                        .child("messages")
                                                        .child(chatMessageList.get(holder.getLayoutPosition()).getMessageID())
                                                        .child("emoticon")
                                                        .setValue(-1);
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(chatMessageList.get(holder.getLayoutPosition()).getMessageID())
                                                        .child("emoticon")
                                                        .setValue(-1);
                                            } else {
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(senderRoom)
                                                        .child("messages")
                                                        .child(chatMessageList.get(holder.getLayoutPosition()).getMessageID())
                                                        .child("emoticon")
                                                        .setValue(index);
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(chatMessageList.get(holder.getLayoutPosition()).getMessageID())
                                                        .child("emoticon")
                                                        .setValue(index);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        return true; // true is closing popup, false is requesting a new selection
                    });

                    receiveViewHolder.itemChatMessageReceiveBinding.llChatReceive.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            popup.onTouch(view, motionEvent);
                            return false;
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (chatMessageList != null) {
            return chatMessageList.size();
        }
        return 0;
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {
        private ItemChatMessageSendBinding itemChatMessageSendBinding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            itemChatMessageSendBinding = ItemChatMessageSendBinding.bind(itemView);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            int half = (width / 2) + (width / 4);
            itemChatMessageSendBinding.tvSend.setMaxWidth(half);
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        private ItemChatMessageReceiveBinding itemChatMessageReceiveBinding;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            itemChatMessageReceiveBinding = ItemChatMessageReceiveBinding.bind(itemView);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            int half = (width / 2) + (width / 4);
            itemChatMessageReceiveBinding.tvReceive.setMaxWidth(half);
        }
    }
}

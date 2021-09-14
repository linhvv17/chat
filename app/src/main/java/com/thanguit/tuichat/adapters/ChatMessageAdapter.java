package com.thanguit.tuichat.adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.thanguit.tuichat.R;
import com.thanguit.tuichat.animations.AnimationScale;
import com.thanguit.tuichat.databinding.ActivityChatBinding;
import com.thanguit.tuichat.databinding.ItemChatMessageReceiveBinding;
import com.thanguit.tuichat.databinding.ItemChatMessageSendBinding;
import com.thanguit.tuichat.databinding.LayoutBottomSheetChatBinding;
import com.thanguit.tuichat.databinding.LayoutTextviewDialogBinding;
import com.thanguit.tuichat.models.ChatMessage;
import com.thanguit.tuichat.utils.MyToast;
import com.thanguit.tuichat.utils.OptionDialog;

import java.util.HashMap;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ChatMessageAdapter";

    private FirebaseAuth firebaseAuth;

    private BottomSheetDialog bottomSheetDialog;
    private Dialog dialog;

    private Context context;
    private List<ChatMessage> chatMessageList;
    private String uid;
    private String avatar;
    private String senderRoom;
    private String receiverRoom;

    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVE = 2;

    private static final String CHAT_SEND = "CHAT_SEND";
    private static final String CHAT_RECEIVE = "CHAT_RECEIVE";

    private FirebaseDatabase firebaseDatabase;

    int[] emoticon = new int[]{
            R.drawable.ic_love,
            R.drawable.ic_laughing,
            R.drawable.ic_crying,
            R.drawable.ic_angry,
            R.drawable.ic_surprised,
            R.drawable.ic_cool
    };

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getUid().trim().equals(chatMessageList.get(position).getSenderID().trim())) {
                return ITEM_SEND;
            } else {
                return ITEM_RECEIVE;
            }
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessageList.get(holder.getLayoutPosition());

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            if (uid.trim().equals(currentUser.getUid().trim())) {
                SendViewHolder sendViewHolder = (SendViewHolder) holder;

                sendViewHolder.itemChatMessageSendBinding.tvTime.setText(chatMessage.getTime().trim());
                if (!chatMessage.getImage().trim().isEmpty()) {
                    handleImage(sendViewHolder.itemChatMessageSendBinding.ivImage, chatMessage.getImage().trim());
                    sendViewHolder.itemChatMessageSendBinding.ivImage.setVisibility(View.VISIBLE);
                    sendViewHolder.itemChatMessageSendBinding.tvSend.setVisibility(View.GONE);
                } else {
                    sendViewHolder.itemChatMessageSendBinding.tvSend.setText(chatMessage.getMessage().trim());
                    sendViewHolder.itemChatMessageSendBinding.ivImage.setVisibility(View.GONE);
                    sendViewHolder.itemChatMessageSendBinding.tvSend.setVisibility(View.VISIBLE);
                }

                sendViewHolder.itemChatMessageSendBinding.llChatSend.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        openChatBottomSheetDialog(CHAT_SEND.trim(), chatMessage);
                        return false;
                    }
                });

            } else {
                ReactionsConfig config = new ReactionsConfigBuilder(context)
                        .withReactions(emoticon)
                        .withPopupColor(context.getResources().getColor(R.color.color_8))
                        .build();

                if (getItemViewType(position) == ITEM_SEND) {
                    SendViewHolder sendViewHolder = (SendViewHolder) holder;

                    sendViewHolder.itemChatMessageSendBinding.tvTime.setText(chatMessage.getTime().trim());
                    if (!chatMessage.getImage().trim().isEmpty()) {
                        handleImage(sendViewHolder.itemChatMessageSendBinding.ivImage, chatMessage.getImage().trim());
                        sendViewHolder.itemChatMessageSendBinding.ivImage.setVisibility(View.VISIBLE);
                        sendViewHolder.itemChatMessageSendBinding.tvSend.setVisibility(View.GONE);
                    } else {
                        sendViewHolder.itemChatMessageSendBinding.tvSend.setText(chatMessage.getMessage().trim());
                        sendViewHolder.itemChatMessageSendBinding.ivImage.setVisibility(View.GONE);
                        sendViewHolder.itemChatMessageSendBinding.tvSend.setVisibility(View.VISIBLE);
                    }

                    if (chatMessage.getEmoticon() >= 0) {
                        sendViewHolder.itemChatMessageSendBinding.ivEmoticon.setImageResource(emoticon[chatMessage.getEmoticon()]);
                        sendViewHolder.itemChatMessageSendBinding.ivEmoticon.setVisibility(View.VISIBLE);
                    } else {
                        sendViewHolder.itemChatMessageSendBinding.ivEmoticon.setVisibility(View.GONE);
                    }

                    sendViewHolder.itemChatMessageSendBinding.llChatSend.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            openChatBottomSheetDialog(CHAT_SEND.trim(), chatMessage);
                            return false;
                        }
                    });
                } else {
                    ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;

                    Picasso.get()
                            .load(avatar)
                            .placeholder(R.drawable.ic_user_avatar)
                            .error(R.drawable.ic_user_avatar)
                            .into(receiveViewHolder.itemChatMessageReceiveBinding.civAvatar);
                    receiveViewHolder.itemChatMessageReceiveBinding.tvTime.setText(chatMessage.getTime().trim());
                    if (!chatMessage.getImage().trim().isEmpty()) {
                        handleImage(receiveViewHolder.itemChatMessageReceiveBinding.ivImage, chatMessage.getImage().trim());
                        receiveViewHolder.itemChatMessageReceiveBinding.ivImage.setVisibility(View.VISIBLE);
                        receiveViewHolder.itemChatMessageReceiveBinding.tvReceive.setVisibility(View.GONE);
                    } else {
                        receiveViewHolder.itemChatMessageReceiveBinding.tvReceive.setText(chatMessage.getMessage().trim());
                        receiveViewHolder.itemChatMessageReceiveBinding.ivImage.setVisibility(View.GONE);
                        receiveViewHolder.itemChatMessageReceiveBinding.tvReceive.setVisibility(View.VISIBLE);
                    }

                    if (chatMessage.getEmoticon() >= 0) {
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setImageResource(emoticon[chatMessage.getEmoticon()]);
                    } else {
                        receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setImageResource(R.drawable.ic_add_1);
                    }

                    receiveViewHolder.itemChatMessageReceiveBinding.llChatReceive.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            openChatBottomSheetDialog(CHAT_RECEIVE.trim(), chatMessage);
                            return false;
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
                                .child(chatMessage.getMessageID())
                                .child("emoticon").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task1) {
                                firebaseDatabase.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(chatMessage.getMessageID())
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
                                                        .child(chatMessage.getMessageID())
                                                        .child("emoticon")
                                                        .setValue(-1);
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(chatMessage.getMessageID())
                                                        .child("emoticon")
                                                        .setValue(-1);
                                            } else {
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(senderRoom)
                                                        .child("messages")
                                                        .child(chatMessage.getMessageID())
                                                        .child("emoticon")
                                                        .setValue(index);
                                                firebaseDatabase.getReference()
                                                        .child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(chatMessage.getMessageID())
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

                    receiveViewHolder.itemChatMessageReceiveBinding.ivEmoticon.setOnTouchListener(new View.OnTouchListener() {
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
        private final ItemChatMessageSendBinding itemChatMessageSendBinding;

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

            itemChatMessageSendBinding.llChatSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemChatMessageSendBinding.tvTime.getVisibility() == View.GONE) {
                        itemChatMessageSendBinding.tvTime.setVisibility(View.VISIBLE);
                    } else {
                        itemChatMessageSendBinding.tvTime.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageReceiveBinding itemChatMessageReceiveBinding;

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

            itemChatMessageReceiveBinding.llChatReceive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemChatMessageReceiveBinding.tvTime.getVisibility() == View.GONE) {
                        itemChatMessageReceiveBinding.tvTime.setVisibility(View.VISIBLE);
                    } else {
                        itemChatMessageReceiveBinding.tvTime.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void handleImage(ImageView ivImage, String image) {
        Picasso.get()
                .load(image.trim())
                .placeholder(R.drawable.ic_picture)
                .error(R.drawable.ic_picture)
                .into(ivImage);
    }

    private void openChatBottomSheetDialog(String layout, ChatMessage chatMessage) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_chat, null);
        LayoutBottomSheetChatBinding layoutBottomSheetChatBinding = LayoutBottomSheetChatBinding.bind(view);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);

        if (layout.equals(CHAT_RECEIVE)) {
            layoutBottomSheetChatBinding.rlRemoveForYou.setVisibility(View.GONE);
            layoutBottomSheetChatBinding.rlRemoveForEveryone.setVisibility(View.GONE);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            if (uid.trim().equals(currentUser.getUid().trim())) {
                layoutBottomSheetChatBinding.rlRemoveForEveryone.setVisibility(View.GONE);
            }
        }

        layoutBottomSheetChatBinding.rlCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", chatMessage.getMessage().trim());
                clipboardManager.setPrimaryClip(clipData);

                MyToast.makeText(context, MyToast.SUCCESS, context.getString(R.string.toast10), MyToast.SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        layoutBottomSheetChatBinding.rlRemoveForYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openConfirmDialog("REMOVE_FOR_YOU", bottomSheetDialog, chatMessage, senderRoom, receiverRoom);
            }
        });

        layoutBottomSheetChatBinding.rlRemoveForEveryone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openConfirmDialog("REMOVE_FOR_EVERYONE", bottomSheetDialog, chatMessage, senderRoom, receiverRoom);
            }
        });

        bottomSheetDialog.show();
    }

    private void openConfirmDialog(String layout, BottomSheetDialog bottomSheetDialog, ChatMessage chatMessage, String senderRoom, String receiverRoom) {
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (layout.equals("REMOVE_FOR_YOU")) {
            OptionDialog removeForYouDialog = new OptionDialog(
                    context,
                    context.getString(R.string.tvRemoveForYou).trim(),
                    context.getString(R.string.tvDialogContent1).trim(),
                    context.getString(R.string.btnDialog11).trim(),
                    context.getString(R.string.btnDialog22),
                    true,
                    new OptionDialog.SetActionButtonListener() {
                        @Override
                        public void setNegativeButtonListener(Dialog dialog) {
                            dialog.dismiss();
                        }

                        @Override
                        public void setPositiveButtonListener(Dialog dialog) {
                            firebaseDatabase.getReference().child("chats").child(senderRoom).child("messages").child(chatMessage.getMessageID())
                                    .removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            if (chatMessageList != null) {
                                                HashMap<String, Object> lastMessageObj = new HashMap<>();
                                                if (chatMessageList.isEmpty()) {
                                                    lastMessageObj.put("lastMessage", context.getString(R.string.tvLastMessage).trim());
                                                } else {
                                                    lastMessageObj.put("lastMessage", chatMessageList.get(chatMessageList.size() - 1).getMessage().trim());
                                                }
                                                firebaseDatabase.getReference().child("chats").child(senderRoom.trim()).updateChildren(lastMessageObj);
                                            }

                                            dialog.dismiss();
                                            bottomSheetDialog.dismiss();
                                            MyToast.makeText(context, MyToast.SUCCESS, context.getString(R.string.toast11), MyToast.SHORT).show();
                                        }
                                    });
                        }
                    }
            );
            removeForYouDialog.show();
        } else {
            OptionDialog removeForEveryoneDialog = new OptionDialog(
                    context,
                    context.getString(R.string.tvRemoveForEveryone).trim(),
                    context.getString(R.string.tvDialogContent2).trim(),
                    context.getString(R.string.btnDialog11).trim(),
                    context.getString(R.string.btnDialog22),
                    true,
                    new OptionDialog.SetActionButtonListener() {
                        @Override
                        public void setNegativeButtonListener(Dialog dialog) {
                            dialog.dismiss();
                        }

                        @Override
                        public void setPositiveButtonListener(Dialog dialog) {
                            firebaseDatabase.getReference().child("chats").child(senderRoom).child("messages").child(chatMessage.getMessageID())
                                    .removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            firebaseDatabase.getReference().child("chats").child(receiverRoom).child("messages").child(chatMessage.getMessageID())
                                                    .removeValue(new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                            if (chatMessageList != null) {
                                                                HashMap<String, Object> lastMessageObj = new HashMap<>();
                                                                if (chatMessageList.isEmpty()) {
                                                                    lastMessageObj.put("lastMessage", context.getString(R.string.tvLastMessage).trim());
                                                                } else {
                                                                    lastMessageObj.put("lastMessage", chatMessageList.get(chatMessageList.size() - 1).getMessage().trim());
                                                                }
                                                                firebaseDatabase.getReference().child("chats").child(senderRoom.trim()).updateChildren(lastMessageObj);
                                                                firebaseDatabase.getReference().child("chats").child(receiverRoom.trim()).updateChildren(lastMessageObj);
                                                            }

                                                            dialog.dismiss();
                                                            bottomSheetDialog.dismiss();
                                                            MyToast.makeText(context, MyToast.SUCCESS, context.getString(R.string.toast11), MyToast.SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
            );
            removeForEveryoneDialog.show();
        }
    }
}

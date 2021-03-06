package com.thanguit.tuichat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.thanguit.tuichat.R;
import com.thanguit.tuichat.activities.ChatActivity;
import com.thanguit.tuichat.databinding.ItemConversationBinding;
import com.thanguit.tuichat.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private static final String TAG = "UserAdapter";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private Context context;
    private List<User> userList;

    public UserAdapter() {
    }

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get()
                .load(userList.get(holder.getLayoutPosition()).getAvatar())
                .placeholder(R.drawable.ic_user_avatar)
                .error(R.drawable.ic_user_avatar)
                .into(holder.itemConversationBinding.civAvatar);
        holder.itemConversationBinding.tvChatName.setText(trim(userList.get(holder.getLayoutPosition()).getName()));

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String senderRoom = trim(currentUser.getUid()) + trim(userList.get(holder.getLayoutPosition()).getUid());

            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference().child("chats")
                    .child(senderRoom)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String lastMessage = snapshot.child("lastMessage").getValue(String.class);
                                String lastMessageTime = snapshot.child("lastMessageTime").getValue(String.class);

                                if (lastMessage != null && lastMessageTime != null) {
                                    holder.itemConversationBinding.tvLastMessage.setText(lastMessage);
                                    holder.itemConversationBinding.tvChatTime.setText(handleDate(lastMessageTime));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("USER", userList.get(holder.getLayoutPosition()));
                context.startActivity(intent);
            }
        });
    }

    private String handleDate(String date) {
        // Code l???y t??? link n??y n??: https://vnsharebox.com/blog/convert-string-to-datetime-android/
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        try {
            Date datetime_1 = simpleDateFormat.parse(date); // Chuy???n String ng??y nh???p v??o th??nh Date

            String currentTime = simpleDateFormat.format(calendar.getTime()); // calendar.getTime(): Tr??? v??? ?????i t?????ng Date d???a tr??n gi?? tr??? c???a Calendar.
            Date datetime_2 = simpleDateFormat.parse(currentTime); // Chuy???n String ng??y nh???p v??o th??nh Date

            long diff = datetime_2.getTime() - datetime_1.getTime();
            int hours = (int) (diff / (60 * 60 * 1000));
            int minutes = (int) (diff / (1000 * 60)) % 60;
            int days = (int) (diff / (24 * 60 * 60 * 1000));

            if (days > 0) {
                return days + context.getString(R.string.days);
            } else {
                if (hours > 0) {
                    return hours + context.getString(R.string.hours);
                } else {
                    return minutes + context.getString(R.string.minutes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.today);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemConversationBinding itemConversationBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemConversationBinding = ItemConversationBinding.bind(itemView);
            itemConversationBinding.tvChatName.setSelected(true);
        }
    }

    public static String trim(final String str) {
        return str == null ? null : str;
    }
}

package com.example.mastermind.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mastermind.R;
import com.example.mastermind.model.ComeBackBroadcast;
import com.example.mastermind.model.Const;
import com.example.mastermind.model.user.CurrentUser;
import com.example.mastermind.model.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference mStorageRef;

    FirebaseDatabase database;
    DatabaseReference myRef;
    User user;
    TextView tv_name;
    TextView tv_coins;
    CircleImageView circleImageView;
    int from;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        user = CurrentUser.getInstance();
        tv_name = findViewById(R.id.tv_name);
        tv_name.setText(user.getName());

        circleImageView = findViewById(R.id.iv_image);
        Glide.with(this).load(user.getImgUrl()).into(circleImageView);

        createNotificationChannel();

        from = getIntent().getIntExtra("from", 0);
        getIntent().removeExtra("from");
        showCoins();
    }

    public void showCoins() {
        tv_coins = findViewById(R.id.textView_coins);
        try {
            FirebaseDatabase.getInstance().getReference().child(Const.USERS_IN_FIREBASE).child(CurrentUser.getInstance().getId()).child(Const.COLLECTION_IN_FIREBASE).child(Const.COINS_IN_FIREBASE).addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int coins = snapshot.getValue(Integer.class);
                        CurrentUser.setUserCoins(coins);
                        if (from == 1) {
                            CurrentUser.addCoins(300);
                            from = 0;
                            Toast.makeText(HomeActivity.this, "Congrats, You got 300 More Coins", Toast.LENGTH_SHORT).show();
                        }
                        tv_coins.setText("" + coins);
                    } else {
                        FirebaseDatabase.getInstance().getReference().child(Const.USERS_IN_FIREBASE).child(CurrentUser.getInstance().getId()).child(Const.COLLECTION_IN_FIREBASE).child(Const.COINS_IN_FIREBASE).setValue(0);
                        CurrentUser.setUserCoins(0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseDatabase.getInstance().getReference().child(Const.USERS_IN_FIREBASE).child(CurrentUser.getInstance().getId()).child(Const.COLLECTION_IN_FIREBASE).child(Const.COINS_IN_FIREBASE).setValue(0);
        }
    }

    public void onClickOnePlayer(View view) {
        Intent intent = new Intent(this, OnePlayerActivity.class);
        startActivity(intent);
    }

    public void onClickTwoPlayer(View view) {
        Intent intent = new Intent(this, MultiplayerActivity.class);
        intent.putExtra(Const.INTENT_EXTRA_KEY_TYPE, Const.INTENT_EXTRA_VALUE_WITH_CODE);
        startActivity(intent);
    }

    public void onClickFindEnemy(View view) {
        Intent intent = new Intent(this, MultiplayerActivity.class);
        intent.putExtra(Const.INTENT_EXTRA_KEY_TYPE, Const.INTENT_EXTRA_VALUE_FIND_ENEMY);
        startActivity(intent);
    }


    public void onClickLogOut(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        mAuth.signOut();
        CurrentUser.logout();
        currentUser = null;
        startActivity(intent);
    }

    public void onClickHowToPlay(View view) {
        Intent intent = new Intent(this, HowToPlayActivity.class);
        startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = Const.NOTIFICATION_CHANNEL_NAME;
            String description = "Click Here To Get Extra 300 Coins";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Const.NOTIFICATION_CHANNEL_NAME, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void onClickThemes(View v) {
        Intent intent = new Intent(this, ThemesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getIntent().removeExtra(Const.INTENT_EXTRA_KEY_FROM);
        Intent intent = new Intent(HomeActivity.this, ComeBackBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long currentTime = System.currentTimeMillis();
        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime + Const.NOTIFICATION_TIME, pendingIntent);
    }
}

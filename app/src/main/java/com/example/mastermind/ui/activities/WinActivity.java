package com.example.mastermind.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mastermind.R;
import com.example.mastermind.model.game.Record;
import com.example.mastermind.model.listeners.DataChangedListener;
import com.example.mastermind.model.user.CurrentUser;
import com.example.mastermind.model.user.User;
import com.example.mastermind.ui.adapters.RecordViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class WinActivity extends AppCompatActivity {

    User currentUser;
    TextView tv_name;
    TextView tv_time;
    CircleImageView profileImage;
    long minutes, seconds;
    long time;

    private FirebaseRecyclerOptions<Record> options;
    private FirebaseRecyclerAdapter<Record, RecordViewHolder> adapter;
    private RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        currentUser = CurrentUser.getInstance();
        tv_name = findViewById(R.id.winner_name);
        tv_time = findViewById(R.id.winner_Time);
        profileImage = findViewById(R.id.profile_image);
        getData();
        calculateCoins();

        recyclerView = findViewById(R.id.recyclerView_records);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        Query ref = FirebaseDatabase.getInstance().getReference().child("Records");
        options = new FirebaseRecyclerOptions.Builder<Record>().setQuery(ref, Record.class).build();
        adapter = new FirebaseRecyclerAdapter<Record, RecordViewHolder>(options) {
            @SuppressLint("DefaultLocale")
            @Override
            protected void onBindViewHolder(@NonNull RecordViewHolder holder, int position, @NonNull Record model) {
                Glide.with(WinActivity.this).load(model.getImgUrl()).into(holder.img);
                holder.name.setText(model.getName());
                long minutes = (model.getTime() / 1000) / 60;
                long seconds = (model.getTime() / 1000) % 60;
                holder.time.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                holder.sn.setText(String.format("%d", position + 1));
            }

            @NonNull
            @Override
            public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_record, parent, false);
                return new RecordViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void calculateCoins() {
        int coins = 50;
        if (time < 180000)
            coins = 100;
        if (time < 120000)
            coins = 125;
        if (time < 90000)
            coins = 150;
        if (time < 60000)
            coins = 200;
        if (time < 30000)
            coins = 300;
        CurrentUser.addCoins(coins);
    }


    public void onClickRestart(View view) {
        Intent intent = new Intent(this, OnePlayerActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void getData() {
        Intent intent = getIntent();
        minutes = intent.getLongExtra("minutes", 0);
        seconds = intent.getLongExtra("seconds", 0);
        time = intent.getLongExtra("time", 0);
        tv_name.setText(currentUser.getName());
        tv_time.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        Glide.with(this).load(currentUser.getImgUrl()).into(profileImage);
        addRecord();

    }

    private void addRecord() {
        final Record record = new Record(time, currentUser.getName(), currentUser.getImgUrl());
        final ArrayList<Record> records = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Records").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dSnapshot: snapshot.getChildren()) {
                    records.add(dSnapshot.getValue(Record.class));
                }
                ArrayList<Record> recordsSorted = sortAndAdd(records,record);
                int currRecord = recordsSorted.indexOf(record);
                HashMap<String,Object> hashMap = getSortedMap(recordsSorted);
                FirebaseDatabase.getInstance().getReference().child("Records").setValue(hashMap);
                recyclerView.smoothScrollToPosition(currRecord);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private HashMap<String,Object> getSortedMap(ArrayList<Record> records){
        HashMap<String,Object> hashMap = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            hashMap.put(""+i,records.get(i));
        }
        return hashMap;
    }

    private ArrayList<Record> sortAndAdd(ArrayList<Record> records ,Record record){
        records.add(record);
        ArrayList<Record> sorted = new ArrayList<>();
        while (!records.isEmpty() && sorted.size() < 10) {
            int index = 0;
            for (int i = 1; i < records.size(); i++) {
                Record curr = records.get(i);
                if (curr.getTime() < records.get(index).getTime()) {
                    index = i;
                }
            }
            sorted.add(records.get(index));
            records.remove(index);
        }
        return sorted;
    }

}
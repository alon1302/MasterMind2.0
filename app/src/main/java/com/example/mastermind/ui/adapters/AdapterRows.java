package com.example.mastermind.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mastermind.R;
import com.example.mastermind.model.Const;
import com.example.mastermind.model.game.CheckRow;
import com.example.mastermind.model.game.GameRow;
import com.example.mastermind.model.listeners.OnPegClickListener;
import com.example.mastermind.model.theme.Themes;
import com.example.mastermind.model.user.CurrentUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterRows extends RecyclerView.Adapter<AdapterRows.ViewHolder> {
    private ArrayList<GameRow> gameRows;
    private ArrayList<CheckRow> checkRows;
    private Context context;
    public LinearLayout fullRow;
    boolean clickable;
    Drawable theme;

    public AdapterRows(ArrayList<GameRow> gameRows, ArrayList<CheckRow> checkRows, Context context, boolean clickable) {
        this.gameRows = gameRows;
        this.checkRows = checkRows;
        this.context = context;
        this.clickable = clickable;
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(Const.SHARED_PREFERENCES_ID + CurrentUser.getInstance().getId(), Context.MODE_PRIVATE);
        int useIndex = sharedPreferences.getInt(Const.SHARED_PREFERENCES_KEY_INDEX, 0);
        int themeImg = Themes.getInstance(context.getApplicationContext()).getAllThemes().get(useIndex).getPegImage();
        theme = context.getResources().getDrawable(themeImg);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRow currGameRow = gameRows.get(position);
        CheckRow currCheckRow = checkRows.get(position);
        String[] colorCheckRow = currCheckRow.getStringRow();
        colorCheckRow = sortCheckRow(colorCheckRow);
        for (int i = 0; i < Const.ROW_SIZE; i++) {
            holder.check[i].setImageResource((Integer) Const.STRING_TO_COLOR_MAP.get(colorCheckRow[i]));
            if (colorCheckRow[i].equals(Const.NULL_COLOR_IN_GAME))
                holder.check[i].setVisibility(View.INVISIBLE);
            else
                holder.check[i].setVisibility(View.VISIBLE);
        }
        String[] colorGameRow = currGameRow.getStringRow();
        for (int i = 0; i < Const.ROW_SIZE; i++) {
            final int finalI = i;
            if (clickable) {
                holder.game[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OnPegClickListener pegClickListener = (OnPegClickListener) context;
                        pegClickListener.onPositionClicked(finalI);
                    }
                });
            }
            holder.game[i].setImageResource((Integer) Const.STRING_TO_COLOR_MAP.get(colorGameRow[i]));
            if (!colorGameRow[i].equals(Const.NULL_COLOR_IN_GAME))
                holder.game[i].setForeground(theme);
            else
                holder.game[i].setForeground(null);
        }
    }

    public String[] sortCheckRow(String[] arr) {
        String[] newArr = new String[Const.ROW_SIZE];
        int black = 0, white = 0;
        for (int i = 0; i < arr.length; i++)
            newArr[i] = Const.NULL_COLOR_IN_GAME;
        for (int i = 0; i < arr.length; i++)
            if (arr[i].equals(Const.BLACK_COLOR_IN_GAME))
                black++;
            else if (arr[i].equals(Const.WHITE_COLOR_IN_GAME))
                white++;
        for (int i = 0; i < black; i++)
            newArr[i] = Const.BLACK_COLOR_IN_GAME;
        for (int i = black; i < black + white; i++)
            newArr[i] = Const.WHITE_COLOR_IN_GAME;
        return newArr;
    }

    @Override
    public int getItemCount() {
        return gameRows.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView[] game;
        public CircleImageView[] check;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            game = new CircleImageView[Const.ROW_SIZE];
            game[0] = itemView.findViewById(R.id.game_0);
            game[1] = itemView.findViewById(R.id.game_1);
            game[2] = itemView.findViewById(R.id.game_2);
            game[3] = itemView.findViewById(R.id.game_3);
            check = new CircleImageView[Const.ROW_SIZE];
            check[0] = itemView.findViewById(R.id.check_0);
            check[1] = itemView.findViewById(R.id.check_1);
            check[2] = itemView.findViewById(R.id.check_2);
            check[3] = itemView.findViewById(R.id.check_3);
            fullRow = itemView.findViewById(R.id.fullRow);
        }
    }
}

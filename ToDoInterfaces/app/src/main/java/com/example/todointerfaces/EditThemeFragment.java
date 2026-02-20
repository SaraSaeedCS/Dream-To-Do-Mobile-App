package com.example.todointerfaces;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.example.todointerfaces.Utils.ThemeManager;

public class EditThemeFragment extends Fragment {
    private Button buttonDarkGreen;
    private Button buttonBabyPink;
    ThemeManager c=new ThemeManager();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_theme, container, false);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.example.light");
        intentFilter.addAction("com.example.dark");
        requireContext().registerReceiver(c,intentFilter,Context.RECEIVER_EXPORTED);

        buttonDarkGreen = view.findViewById(R.id.button_dark_green);
        buttonBabyPink  = view.findViewById(R.id.button_baby_pink);
        buttonDarkGreen.setOnClickListener(v -> {
            Intent i=new Intent("com.example.dark");
            requireContext().sendBroadcast(i);

        });
        buttonBabyPink.setOnClickListener(v -> {
            Intent i=new Intent("com.example.light");
            requireContext().sendBroadcast(i);

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(c);
    }
}

package com.example.todointerfaces;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class aboutFragment extends Fragment {
    public aboutFragment() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_about, container, false);
        TextView versionText = view.findViewById(R.id.appVersion);
        TextView contactText = view.findViewById(R.id.contactText);
        Context context=getContext();
        if(context!=null){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionText.setText("Version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("Version 1.0");
        }}
        contactText.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@hooppa.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "ToDo Interfaces - Support");

            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (Exception e) {
                Toast.makeText(context, "No email app installed", Toast.LENGTH_SHORT).show();
            }

        } );
        return view;
    }
}
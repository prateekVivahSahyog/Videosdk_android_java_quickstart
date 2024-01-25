package live.videosdk.rtc.android.quickstart;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import live.videosdk.rtc.android.VideoSDK;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

//        FirebaseApp.initializeApp(getApplicationContext());
//        FirebaseDatabase.getInstance().getReference().child("prateek").setValue("hello");
        VideoSDK.initialize(getApplicationContext());
        // Initialize Firebase if it's not already initialized


    }
}
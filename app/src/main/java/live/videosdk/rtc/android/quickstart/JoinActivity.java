package live.videosdk.rtc.android.quickstart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import live.videosdk.rtc.android.quickstart.repository.MainRepository;
import live.videosdk.rtc.android.quickstart.utils.DataModelType;
import live.videosdk.rtc.android.quickstart.utils.RoomIdCallback;


/// vivhasahyog wala commit
public class JoinActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private MainRepository mainRepository;

    //Replace with the token you generated from the VideoSDK Dashboard
    String sampleToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiIwMzVjNmIxNC04MzQ2LTRkYzgtYjBmMC1kNGQ4ZWQwOTMyODAiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTcwNDgwODM0NCwiZXhwIjoxODYyNTk2MzQ0fQ.3lxWgCRv-o55JU_iMDAaW9JtPDVYXL4-VJ6kst349lc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        mainRepository = MainRepository.getInstance();

        final Button btnCreate = findViewById(R.id.btnCreateMeeting);
        final ImageView btnAccept = findViewById(R.id.acceptButton);
        final ImageView btnReject = findViewById(R.id.rejectButton);
        final TextView  incomingPerson = findViewById(R.id.incomingNameTV);
        final LinearLayout callLayout = findViewById(R.id.incomingCallLayout);

        final EditText participantName = findViewById(R.id.etParticipantName);

        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID);
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID);

        btnCreate.setOnClickListener(v -> {
            Log.d("xxx", "button pressed");

            String participantNameStr = participantName.getText().toString();
            Log.d("xxx", "participant name "+participantNameStr);

            createMeeting(sampleToken, meetingID -> {
                // roomId is available here

                Log.d("xxx","Meeting ID "+meetingID);

                if(meetingID!=null){
                    Log.d("xxx", "IN JoinActivity main Repoca send call called ");
                    mainRepository.sendCallRequest(participantNameStr,meetingID,()->{
                        Toast.makeText(JoinActivity.this, "couldn't  find person", Toast.LENGTH_SHORT).show();
                    });
                    Intent intent = new Intent(JoinActivity.this, MeetingActivity.class);
                    intent.putExtra("token", sampleToken);
                    intent.putExtra("meetingId", meetingID);
                    intent.putExtra("participantName",participantNameStr);
                    startActivity(intent);
                }
                else {
                    Log.d("xxx","now meeting id is null");
                }

            });

        });

        mainRepository.subscribeForLatestEvents(data->{
                    if(data.getType() == DataModelType.StartCall){
                      incomingPerson.setText(data.getSender()+" is calling you");
                      callLayout.setVisibility(View.VISIBLE);

                        btnAccept.setOnClickListener(v->{
                            callLayout.setVisibility(View.GONE);

                            Intent intent = new Intent(JoinActivity.this, MeetingActivity.class);
                            intent.putExtra("token", sampleToken);
                            intent.putExtra("meetingId", data.getMeetingID());
                            intent.putExtra("participantName",data.getSender());
                            startActivity(intent);
                                }
                        );
                        btnReject.setOnClickListener(v->{
                            callLayout.setVisibility(View.GONE);
                                }
                        );
                    }
        }

        );


    }

    private void createMeeting(String token, RoomIdCallback callback) {
        AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
                .addHeaders("Authorization", token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String roomId = response.getString("roomId");
                            callback.onRoomIdReceived(roomId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(JoinActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Toast.makeText(JoinActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }
}

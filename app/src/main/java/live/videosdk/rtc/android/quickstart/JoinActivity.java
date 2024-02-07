package live.videosdk.rtc.android.quickstart;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import live.videosdk.rtc.android.quickstart.repository.MainRepository;
import live.videosdk.rtc.android.quickstart.utils.RoomIdCallback;
import live.videosdk.rtc.android.quickstart.utils.StatusType;


/// vivhasahyog wala commit

public class JoinActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private MainRepository mainRepository;

    Button btnCreate ;
    ImageView btnAccept ;
    ImageView btnReject ;
    TextView  incomingPerson;
    LinearLayout callLayout ;

    RelativeLayout DialerScreen;
    RelativeLayout DialingScreen ;
    TextView personName  ;
    ImageView DialingCut ;
    EditText participantName;

    //Replace with the token you generated from the VideoSDK Dashboard
    String sampleToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiIwMzVjNmIxNC04MzQ2LTRkYzgtYjBmMC1kNGQ4ZWQwOTMyODAiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTcwNDgwODM0NCwiZXhwIjoxODYyNTk2MzQ0fQ.3lxWgCRv-o55JU_iMDAaW9JtPDVYXL4-VJ6kst349lc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        btnCreate = findViewById(R.id.btnCreateMeeting);
        btnAccept = findViewById(R.id.acceptButton);
        btnReject = findViewById(R.id.rejectButton);
        incomingPerson = findViewById(R.id.incomingNameTV);
        callLayout = findViewById(R.id.incomingCallLayout);

        DialerScreen = findViewById(R.id.DialerScreen);
        DialingScreen = findViewById(R.id.DialingScreen);
        personName  = findViewById(R.id.personName);
        DialingCut  = findViewById(R.id.dialingCut);
        participantName = findViewById(R.id.call_to);

        mainRepository = MainRepository.getInstance();



        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID);
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID);

        btnCreate.setOnClickListener(v -> {
            keyboard.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            Log.d("xxx", "button pressed");

            String participantNameStr = participantName.getText().toString();
            Log.d("xxx", "participant name "+participantNameStr);

            personName.setText(participantNameStr);

            createMeeting(sampleToken, meetingID -> {
                // roomId is available here
                DialingScreen.setVisibility(View.VISIBLE);
                DialerScreen.setVisibility(View.GONE);

                Log.d("xxx","Meeting ID and visibility  "+meetingID +" "+ DialingScreen.getVisibility());

                if(meetingID!=null){
                    Log.d("xxx", "IN JoinActivity main Repoca send call called ");
                    mainRepository.sendCallRequest(participantNameStr, meetingID, new StatusCallback() {
                        @Override
                        public void onUserBusy() {
                            // Handle the case when the user is busy
                            DialerScreen.setVisibility(View.GONE);
                            DialingScreen.setVisibility(View.VISIBLE);
                            Toast.makeText(JoinActivity.this, "The person is busy", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onUserOffline() {
                            // Handle the case when the user is offline
                            DialerScreen.setVisibility(View.GONE);
                            DialingScreen.setVisibility(View.VISIBLE);
                            Toast.makeText(JoinActivity.this, "The person is offline", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Log.d("xxx"," onError() of StatusCallback ");
                        }
                    }
                    );

                }
                else {
                    Log.d("xxx","now meeting id is null");
                }

            });

        });

        DialingCut.setOnClickListener(v->{
            DialerScreen.setVisibility(View.VISIBLE);
            DialingScreen.setVisibility(View.GONE);
            mainRepository.sendResponseClosed(participantName.getText().toString(),()->{
            });
        });

        mainRepository.subscribeForLatestEvents(data->{

                    switch (data.getType()){

                        case Invited:

                            Log.d("xxx",data.getSender() +" has invited you in VideoCall");
                            mainRepository.setMyStatus(StatusType.Busy);

                            incomingPerson.setText(data.getSender()+" Video Calling");
                            callLayout.setVisibility(View.VISIBLE);

                            btnAccept.setOnClickListener(v->{

                                callLayout.setVisibility(View.GONE);

                                mainRepository.sendResponseAccepted(data.getSender(), data.getTarget(), data.getMeetingID(), new StatusCallback() {
                                    @Override
                                    public void onUserBusy() {

                                    }

                                    @Override
                                    public void onUserOffline() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.d("xxx","error in sendResponseAccepted to "+data.getSender());
                                    }
                                });

                                Intent intent = new Intent(JoinActivity.this, MeetingActivity.class);
                                intent.putExtra("token", sampleToken);
                                intent.putExtra("meetingId", data.getMeetingID());
                                intent.putExtra("Remote",data.getTarget());
                                intent.putExtra("Local",data.getSender());
                                startActivity(intent);
                            });


                            btnReject.setOnClickListener(v->{
                                mainRepository.sendResponseRejected(data.getSender(),data.getTarget(),data.getMeetingID(), new StatusCallback() {
                                            @Override
                                            public void onUserBusy() {

                                            }

                                            @Override
                                            public void onUserOffline() {

                                            }

                                            @Override
                                            public void onError() {
                                                Log.d("xxx","error in sending Rejected response to "+data.getSender());
                                            }
                                        }
                                );
                                mainRepository.setMyStatus(StatusType.Idle);
                                callLayout.setVisibility(View.GONE);
                            });
                            break;


                        case Accepted:
                            DialerScreen.setVisibility(View.GONE);
                            DialingScreen.setVisibility(View.GONE);
                            Log.d("xxx",data.getSender() +" has accepted  the call");
                            Intent intent = new Intent(JoinActivity.this, MeetingActivity.class);
                            intent.putExtra("token", sampleToken);
                            intent.putExtra("meetingId", data.getMeetingID());
                            intent.putExtra("Remote",data.getTarget());
                            intent.putExtra("Local",data.getSender());
                            startActivity(intent);
                            mainRepository.setMyStatus(StatusType.Busy);
                            break;


                        case Rejected:
                            DialerScreen.setVisibility(View.VISIBLE);
                            DialingScreen.setVisibility(View.GONE);
                            Log.d("xxx",data.getSender() +" has rejected the call");
                            mainRepository.setMyStatus(StatusType.Idle);
                            break;

                        case Ended:
                            DialerScreen.setVisibility(View.VISIBLE);
                            DialingScreen.setVisibility(View.GONE);
                            mainRepository.setMyStatus(StatusType.Idle);
                            break;

                        case Busy:
                            mainRepository.setMyStatus(StatusType.Idle);
                            DialerScreen.setVisibility(View.VISIBLE);
                            DialingScreen.setVisibility(View.GONE);
                            Toast.makeText(this, data.getTarget() +" is busy", Toast.LENGTH_LONG).show();
                            break;

                        case  Cancelled:
//                            mainRepository.setMyStatus(StatusType.Ideal);
                            callLayout.setVisibility(View.GONE);
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("JoinActivity", "onPause - Activity is going into the background");


    }

    @Override
    protected void onResume() {
        mainRepository.setMyStatus(StatusType.Idle);
        super.onResume();
        Log.d("JoinActivity", "onResume - Activity is coming back into the foreground");
        participantName.setText("");

    }

    @Override
    protected void onDestroy() {
        mainRepository.setMyStatus(StatusType.Offline);
        super.onDestroy();
        Log.d("JoinActivity", "Activity is being destroyed");
    }

}


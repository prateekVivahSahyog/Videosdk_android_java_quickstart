package live.videosdk.rtc.android.quickstart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import live.videosdk.rtc.android.CustomStreamTrack;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class MeetingActivity extends AppCompatActivity {
    // declare the variables we will be using to handle the meeting
    private Meeting meeting;
    private boolean micEnabled = true;
    private boolean webcamEnabled = true;

//    private boolean camera = false ;

    private RecyclerView rvParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        final String token = getIntent().getStringExtra("token");
        final String meetingId = getIntent().getStringExtra("meetingId");
        final String participantName = getIntent().getStringExtra("participantName");

        // 1. Configuration VideoSDK with Token
        VideoSDK.config(token);
        // 2. Initialize VideoSDK Meeting

        // Custom Video Track
        Map<String, CustomStreamTrack> customTracks = new HashMap<>();
        CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h1080p_w1440p","front", CustomStreamTrack.VideoMode.MOTION, true,this);
        customTracks.put("video", videoCustomTrack);


        meeting = VideoSDK.initMeeting(
                MeetingActivity.this, meetingId, participantName,
                micEnabled, webcamEnabled,null, null, false, customTracks);

        // 3. Add event listener for listening upcoming events
        assert meeting != null;
        meeting.addEventListener(meetingEventListener);

        //4. Join VideoSDK Meeting
        meeting.join();

        ((TextView)findViewById(R.id.tvMeetingId)).setText(meetingId);

        // actions
        setActionListeners();

        rvParticipants = findViewById(R.id.rvParticipants);
        rvParticipants.setLayoutManager(new GridLayoutManager(this, 2));
        rvParticipants.setAdapter(new ParticipantAdapter(meeting));
    }

    // creating the MeetingEventListener
    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @Override
        public void onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()");
        }

        @Override
        public void onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()");
            meeting = null;
           if (!isDestroyed()) finish();
        }

        @Override
        public void onParticipantJoined(Participant participant) {
            Toast.makeText(MeetingActivity.this, participant.getDisplayName() + " joined", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onParticipantLeft(Participant participant) {
            Toast.makeText(MeetingActivity.this, participant.getDisplayName() + " left", Toast.LENGTH_SHORT).show();
        }
    };

    private void setActionListeners() {

        ImageButton btnMic = findViewById(R.id.btnMic);
        btnMic.setOnClickListener(view -> {
            if (micEnabled) {
                // this will mute the local participant's mic
                meeting.muteMic();
                Toast.makeText(MeetingActivity.this, "Mic Disabled", Toast.LENGTH_SHORT).show();
                // set the image to the 'mic off' drawable resource
                btnMic.setImageResource(R.drawable.microphone_off);
            } else {
                // this will unmute the local participant's mic
                meeting.unmuteMic();
                Toast.makeText(MeetingActivity.this, "Mic Enabled", Toast.LENGTH_SHORT).show();
                // set the image to the 'mic on' drawable resource
                btnMic.setImageResource(R.drawable.microphone_on);
            }
            micEnabled = !micEnabled;
        });

        // toggle webcam
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(view -> {
            if (webcamEnabled) {

                meeting.disableWebcam();
                Toast.makeText(MeetingActivity.this, "Webcam Disabled", Toast.LENGTH_SHORT).show();
                btnCamera.setImageResource(R.drawable.no_video);
            } else {

                meeting.enableWebcam();
                Toast.makeText(MeetingActivity.this, "Webcam Enabled", Toast.LENGTH_SHORT).show();

                btnCamera.setImageResource(R.drawable.video_camera_on);
            }
            webcamEnabled = !webcamEnabled;
        });

        // leave meeting
        findViewById(R.id.btnLeave).setOnClickListener(view -> {
            // this will make the local participant leave the meeting
            meeting.leave();
        });
    }

    @Override
    protected void onDestroy() {
        rvParticipants.setAdapter(null);
        super.onDestroy();
    }
}

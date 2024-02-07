package live.videosdk.rtc.android.quickstart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import live.videosdk.rtc.android.CustomStreamTrack;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoSDK;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.lib.PubSubMessage;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;
import live.videosdk.rtc.android.listeners.PubSubMessageListener;
import live.videosdk.rtc.android.quickstart.repository.MainRepository;
//import live.videosdk.rtc.android.quickstart.utils.HelperClass;
import live.videosdk.rtc.android.quickstart.utils.StatusType;

public class MeetingActivity extends AppCompatActivity  {
    // Constants
    private static final String TAG = "#meeting";

    // Instance Variables
    private Meeting meeting;
    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private VideoView localView;
    private VideoView remoteView;
    private AudioManager audioManager;
    private BluetoothAdapter bluetoothAdapter;
    private  String RemoteName ="";
    private String LocalName = "";
    private Boolean ChatboxOpen = false;

    //    private Participant participantRemote;
//    private Participant participantLocal;
    private VideoTrack participantTrack = null;
    private VideoTrack localTrack = null;
    private boolean useBluetooth = true;
    private PubSubMessageListener chatListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        // Initialize UI components
        initializeUI();

        // Initialize audio and Bluetooth
        initializeAudioAndBluetooth();

        // Configure and initialize video SDK
        configureAndInitializeVideoSDK();

        // Set action listeners
        setActionListeners();

        // Register Bluetooth receiver
        registerBluetoothReceiver();
    }

    private void initializeUI() {
        localView = findViewById(R.id.localView);
        remoteView = findViewById(R.id.remoteView);
        // Other UI initialization...
    }

    private void initializeAudioAndBluetooth() {
        // Initialize AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void configureAndInitializeVideoSDK() {
        // Configuration VideoSDK with Token
        VideoSDK.config(getIntent().getStringExtra("token"));

        // Initialize VideoSDK Meeting
        initializeVideoSDKMeeting();
        // Other video SDK configurations...
    }

    private void initializeVideoSDKMeeting() {
        Map<String, CustomStreamTrack> customTracks = new HashMap<>();
        CustomStreamTrack videoCustomTrack = VideoSDK.createCameraVideoTrack("h720p_w1280p", "front", CustomStreamTrack.VideoMode.DETAIL, true, this);
        CustomStreamTrack audioCustomTrack= VideoSDK.createAudioTrack("high_quality", this);
        customTracks.put("video", videoCustomTrack);
        customTracks.put("mic",audioCustomTrack);
        RemoteName = getIntent().getStringExtra("Remote");
        LocalName = getIntent().getStringExtra("Local");

        meeting = VideoSDK.initMeeting(
                MeetingActivity.this,
                getIntent().getStringExtra("meetingId"),
                RemoteName
                ,
                micEnabled,
                webcamEnabled,
                null,
                null,
                true,
                customTracks
        );



        // Add event listener for meeting events
        if (meeting != null) {
            meeting.addEventListener(meetingEventListener);
        }

        // Join VideoSDK Meeting
        if (meeting != null) {
            meeting.join();
        }

        // Set meeting ID text

        String meetingId = getIntent().getStringExtra("meetingId");
        String meetingText = "Meeting ID : "+ meetingId;
        ((TextView) findViewById(R.id.tvMeetingId)).setText(meetingText);


        // Copy meeting ID to clipboard
        //copyMeetingIdToClipboard(meetingId);
    }

    private void copyMeetingIdToClipboard(String meetingId) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Meeting ID", meetingId);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(this, "Meeting link copied to clipboard", Toast.LENGTH_LONG).show();
        }
    }

    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {

        @Override
        public void onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()");
            setLocalListeners();
//            chatListener = new PubSubMessageListener() {
//
//                @Override
//                public void onMessageReceived(PubSubMessage pubSubMessage) {
//                    if (!pubSubMessage.getSenderId().equals(meeting.getLocalParticipant().getId())) {
//                        View parentLayout = findViewById(android.R.id.content);
//                        Snackbar snackbar =
//                                Snackbar.make(parentLayout, pubSubMessage.getSenderName() + " says: " +
//                                                pubSubMessage.getMessage(), Snackbar.LENGTH_SHORT)
//                                        .setDuration(2000);
//                        View snackbarView = snackbar.getView();
//                        HelperClass.setSnackBarStyle(snackbarView, 0);
//                        snackbar.getView().setOnClickListener(view -> snackbar.dismiss());
//                        snackbar.show();
//                    }
//                }
//            };
//
//            meeting.pubSub.subscribe("CHAT", chatListener);

        }

        @Override
        public void onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()");
            meeting.end();

            if (!isDestroyed()) finish();
        }

        @Override
        public void onParticipantJoined(Participant participant) {
            Toast.makeText(MeetingActivity.this, participant.getDisplayName() + " joined", Toast.LENGTH_SHORT).show();
            remoteView.setVisibility(View.VISIBLE);
            participant.addEventListener(participantEventListener);
            ( (TextView) findViewById(R.id.etParticipantName)).setText(participant.getDisplayName());
        }


        @Override
        public void onParticipantLeft(Participant participant) {
            Toast.makeText(MeetingActivity.this, participant.getDisplayName() + " left", Toast.LENGTH_SHORT).show();
//            remoteView.removeTrack();
            participant.removeEventListener(participantEventListener);
            ( (TextView) findViewById(R.id.etParticipantName)).setText("");
            meeting.end();

        }

        @Override
        public void onError(JSONObject error) {
            try {
                JSONObject errorCodes = VideoSDK.getErrorCodes();
                int code = error.getInt("code");
                Log.d("#error", "Error is: " + error.get("message"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private final ParticipantEventListener participantEventListener = new ParticipantEventListener() {
        @Override
        public void onStreamEnabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                if (meeting.getParticipants().size() < 2) {
                    participantTrack = (VideoTrack) stream.getTrack();
                    remoteView.setVisibility(View.VISIBLE);
                    remoteView.addTrack(participantTrack);
                    setQuality();
                }
            }
        }

        @Override
        public void onStreamDisabled(Stream stream) {
            if (stream.getKind().equalsIgnoreCase("video")) {
                if (meeting.getParticipants().size() < 2) {
                    VideoTrack track = (VideoTrack) stream.getTrack();
                    if (track != null) participantTrack = null;
                    remoteView.removeTrack();
                    //  remoteView.setVisibility(View.GONE);
                }
            }

        }
    };

    private void setQuality() {
        final Iterator<Participant> participants = meeting.getParticipants().values().iterator();
        for (int i = 0; i < meeting.getParticipants().size(); i++) {
            Participant participant = participants.next();
            participant.setQuality("high");
        }
    }

    private void setLocalListeners() {

        if (meeting != null && meeting.getLocalParticipant() != null) {
            meeting.getLocalParticipant().addEventListener(new ParticipantEventListener() {

               @Override
               public void onStreamEnabled(Stream stream) {
                   if (stream.getKind().equalsIgnoreCase("video")) {
                       localTrack = (VideoTrack) stream.getTrack();
                       localView.setVisibility(View.VISIBLE);
                       localView.setZOrderMediaOverlay(true);
                       localView.addTrack(localTrack);
                   }
               }


               @Override
               public void onStreamDisabled(Stream stream) {
                   if (stream.getKind().equalsIgnoreCase("video")) {
                       VideoTrack track = (VideoTrack) stream.getTrack();
                       if (track != null) localTrack = null;
                       localView.removeTrack();


                   }
               }
           }
            );
        }
    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                // Bluetooth device connected, update UI or take necessary actions
                Toast.makeText(context, "Bluetooth device connected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // Bluetooth device disconnected, update UI or take necessary actions
                Toast.makeText(context, "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setActionListeners() {
        setMicButtonListener();
        setCameraButtonListener();
        setCameraFlipButtonListener();
        setLeaveButtonListener();
        setAudioButtonListener();
       // setChatButton();
        //setVideoQuality();

    }

    //    private void  setVideoQuality(){
//        Spinner spinner = findViewById(R.id.spinnerVideoQuality);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.video_quality_options, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener();
//    }

//    private void setChatButton(){
//        ImageButton btnChat = findViewById(R.id.chatBtn);
//
//        btnChat.setOnClickListener(v->{
//            if (meeting != null) {
//                openChat();
//            }
//        });
//
//    }

//    @SuppressLint("ClickableViewAccessibility")
//    public void openChat() {
//        RecyclerView messageRcv;
//        ImageView close;
//        bottomSheetDialog = new BottomSheetDialog(this);
//        View v3 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.chat, findViewById(R.id.layout_chat));
//        bottomSheetDialog.setContentView(v3);
//
//        messageRcv = v3.findViewById(R.id.messageRcv);
//        messageRcv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//
//        RelativeLayout.LayoutParams lp =
//                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getWindowHeight() / 2);
//        messageRcv.setLayoutParams(lp);
//
//        BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback
//                = new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet,
//                                       @BottomSheetBehavior.State int newState) {
//                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    RelativeLayout.LayoutParams lp =
//                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getWindowHeight() / 2);
//                    messageRcv.setLayoutParams(lp);
//                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                    RelativeLayout.LayoutParams lp =
//                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//                    messageRcv.setLayoutParams(lp);
//                }
//
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//            }
//        };
//
//        bottomSheetDialog.getBehavior().addBottomSheetCallback(mBottomSheetCallback);
//
//        etmessage = v3.findViewById(R.id.etMessage);
//        etmessage.setOnTouchListener(new View.OnTouchListener() {
//
//            public boolean onTouch(View view, MotionEvent event) {
//                // TODO Auto-generated method stub
//                if (view.getId() == R.id.etMessage) {
//                    view.getParent().requestDisallowInterceptTouchEvent(true);
//                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                        case MotionEvent.ACTION_UP:
//                            view.getParent().requestDisallowInterceptTouchEvent(false);
//                            break;
//                    }
//                }
//                return false;
//            }
//        });
//
//        ImageButton btnSend = v3.findViewById(R.id.sendBtn);
//        btnSend.setEnabled(false);
//        etmessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    etmessage.setHint("");
//                }
//            }
//        });
//
//        etmessage.setVerticalScrollBarEnabled(true);
//        etmessage.setScrollbarFadingEnabled(false);
//
//        etmessage.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (!etmessage.getText().toString().trim().isEmpty()) {
//                    btnSend.setEnabled(true);
//                    btnSend.setSelected(true);
//                } else {
//                    btnSend.setEnabled(false);
//                    btnSend.setSelected(false);
//                }
//            }
//
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//
//        //
//        pubSubMessageListener = new PubSubMessageListener() {
//            @Override
//            public void onMessageReceived(PubSubMessage message) {
//                messageAdapter.addItem(message);
//                messageRcv.scrollToPosition(messageAdapter.getItemCount() - 1);
//            }
//        };
//
//        // Subscribe for 'CHAT' topic
//        List<PubSubMessage> pubSubMessageList = meeting.pubSub.subscribe("CHAT", pubSubMessageListener);
//
//        //
//        messageAdapter = new MessageAdapter(this, R.layout.item_message_list, pubSubMessageList, meeting);
//        messageRcv.setAdapter(messageAdapter);
//        messageRcv.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) ->
//                messageRcv.scrollToPosition(messageAdapter.getItemCount() - 1));
//
//        v3.findViewById(R.id.btnSend).setOnClickListener(view -> {
//            String message = etmessage.getText().toString();
//            if (!message.equals("")) {
//                PubSubPublishOptions publishOptions = new PubSubPublishOptions();
//                publishOptions.setPersist(true);
//
//                meeting.pubSub.publish("CHAT", message, publishOptions);
//                etmessage.setText("");
//            } else {
//                Toast.makeText(OneToOneCallActivity.this, "Please Enter Message",
//                        Toast.LENGTH_SHORT).show();
//            }
//
//        });
//
//
//        close = v3.findViewById(R.id.ic_close);
//        bottomSheetDialog.show();
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//
//        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                meeting.pubSub.unsubscribe("CHAT", pubSubMessageListener);
//            }
//        });
//
//    }
    private void setMicButtonListener() {
        ImageButton btnMic = findViewById(R.id.btnMic);
        btnMic.setOnClickListener(view -> {
            if (micEnabled) {
                muteMicAction(btnMic);
            } else {
                unmuteMicAction(btnMic);
            }
            micEnabled = !micEnabled;
        });
    }

    private void muteMicAction(ImageButton btnMic) {
        meeting.muteMic();
        Toast.makeText(MeetingActivity.this, "Mic Disabled", Toast.LENGTH_SHORT).show();
        btnMic.setImageResource(R.drawable.microphone_off);
    }

    private void unmuteMicAction(ImageButton btnMic) {
        meeting.unmuteMic();
        Toast.makeText(MeetingActivity.this, "Mic Enabled", Toast.LENGTH_SHORT).show();
        btnMic.setImageResource(R.drawable.microphone_on);
    }

    private void setCameraButtonListener() {
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(view -> {
            if (webcamEnabled) {
                disableWebcamAction(btnCamera);
            } else {
                enableWebcamAction(btnCamera);
            }
            webcamEnabled = !webcamEnabled;
        });
    }

    private void disableWebcamAction(ImageButton btnCamera) {
        meeting.disableWebcam();
        Toast.makeText(MeetingActivity.this, "Webcam Disabled", Toast.LENGTH_SHORT).show();
        btnCamera.setImageResource(R.drawable.no_video);
    }

    private void enableWebcamAction(ImageButton btnCamera) {
        meeting.enableWebcam();
        Toast.makeText(MeetingActivity.this, "Webcam Enabled", Toast.LENGTH_SHORT).show();
        btnCamera.setImageResource(R.drawable.video_camera_on);
    }

    private void setCameraFlipButtonListener() {
        ImageButton cameraFlip = findViewById(R.id.btnCameraFlip);
        cameraFlip.setOnClickListener(view -> {
            meeting.changeWebcam();
        });
    }

    private void setLeaveButtonListener() {
        MainRepository mainRepository = MainRepository.getInstance();
        mainRepository.sendResponseEnded(RemoteName,LocalName,meeting.getMeetingId(),()->{

        });
        findViewById(R.id.btnLeave).setOnClickListener(view -> {
            meeting.leave();
        });

    }

    private void setAudioButtonListener() {
        ImageButton audioButton = findViewById(R.id.audio_Output);
        audioButton.setOnClickListener(v -> {
            toggleAudioOutputDevice(audioButton);
        });
    }

    private void toggleAudioOutputDevice(ImageButton audioButton) {
        if (useBluetooth) {
            switchToBluetooth(audioButton);
        } else {
            switchToSpeaker(audioButton);
        }
        useBluetooth = !useBluetooth;
    }

    private void switchToSpeaker(ImageButton audioButton) {
        changeAudioOutputDevice(false); // Change to speaker
        audioButton.setImageResource(R.drawable.speaker);
    }

    private void switchToBluetooth(ImageButton audioButton) {
        changeAudioOutputDevice(true); // Change to Bluetooth
        audioButton.setImageResource(R.drawable.bluetooth_speaker);
    }

    private void changeAudioOutputDevice(boolean useBluetooth) {
        if (audioManager != null) {
            int audioOutputType = useBluetooth ?
                    AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL;

            audioManager.setMode(audioOutputType);

            // Optionally, adjust the stream volume as well
            int streamType = AudioManager.STREAM_VOICE_CALL; // Adjust according to your needs
            audioManager.setStreamVolume(streamType, audioManager.getStreamMaxVolume(streamType), 0);

            // Set the audio device to Bluetooth if required
            if (useBluetooth && bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                for (AudioDeviceInfo device : audioDevices) {
                    if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                        audioManager.setBluetoothScoOn(true);
                        audioManager.startBluetoothSco();
                        audioManager.setSpeakerphoneOn(false);
                        Toast.makeText(this, "Bluetooth device connected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // If no Bluetooth SCO device is found, you may want to handle this case.
                Toast.makeText(this, "No Bluetooth SCO device found", Toast.LENGTH_SHORT).show();
            } else {
                // Use the default device (e.g., speaker)
                audioManager.setBluetoothScoOn(false);
                audioManager.stopBluetoothSco();
                audioManager.setSpeakerphoneOn(true);
            }
        }
    }




    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothReceiver);
        localView.release();
        remoteView.release();
        super.onDestroy();
    }

//    @Override
//    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> parent) {
//
//    }
}

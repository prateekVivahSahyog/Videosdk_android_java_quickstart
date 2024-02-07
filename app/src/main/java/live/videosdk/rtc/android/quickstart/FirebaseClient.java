package live.videosdk.rtc.android.quickstart;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import live.videosdk.rtc.android.quickstart.utils.DataModel;
import live.videosdk.rtc.android.quickstart.utils.ErrorCallBack;
import live.videosdk.rtc.android.quickstart.utils.NewEventCallBack;
import live.videosdk.rtc.android.quickstart.utils.StatusType;
import live.videosdk.rtc.android.quickstart.utils.SuccessCallBack;

public class FirebaseClient {

    private  final Gson gson = new Gson();
    private final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
    private String currentUsername;
    private static final String LATEST_EVENTS_FIELD_NAME = "latest_event";
    private static final String  MY_STATUS = "my_status";

    public  void login(String username, SuccessCallBack callBack){
        dbref.child(username).setValue("").addOnCompleteListener(task->{
           currentUsername = username;
           callBack.onSuccess();
        });


    }

    public  void SetStatus(StatusType currentStatus){
        dbref.child(currentUsername).child(MY_STATUS).setValue(gson.toJson(currentStatus));
    }

    public void sendResponseToMe(DataModel dataModel){
        dbref.child(dataModel.getTarget()).child(LATEST_EVENTS_FIELD_NAME)
                .setValue(gson.toJson(dataModel));
    }

    public void sendMessageToOtherUser(DataModel dataModel, StatusCallback CallBack) {
        Log.d("xxx", "IN FirebaseClient inside sendMessageToOtherUser ");
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(dataModel.getTarget()).exists()) {
                    // Check the status of the user
                    String statusJson = dataSnapshot.child(dataModel.getTarget()).child(MY_STATUS).getValue(String.class);
                    StatusType status = gson.fromJson(statusJson, StatusType.class);
                    if (status == StatusType.Idle) {
                        // Send the signal to the other user
                        dbref.child(dataModel.getTarget()).child(LATEST_EVENTS_FIELD_NAME)
                                .setValue(gson.toJson(dataModel));

                    } else if(status == StatusType.Busy) {
                        Log.d("xxx", "User status is not ideal");
                       CallBack.onUserBusy();
                    }
                    else if(status == StatusType.Offline) {
                        Log.d("xxx", "User does not exist");
                        CallBack.onUserOffline();
                    }

                } else {
                    Log.d("xxx", "User does not exist");
                    CallBack.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("xxx", "errorCallBack at onCancelled: sendMessageToOtherUser");
                CallBack.onError();
            }
        });
    }



    public  void sendResponseEnded(DataModel dataModel, ErrorCallBack errorCallBack){
        Log.d("xxx", "IN FirebaseClient inside sendMessageToOtherUser ");
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(dataModel.getTarget()).exists()){
                    // send Signal to other user
                    dbref.child(dataModel.getTarget()).child(LATEST_EVENTS_FIELD_NAME)
                            .setValue(gson.toJson(dataModel));
                }

                else {
                    Log.d("xxx","  errorCallBack at onDataChange:  sendMessageToOtherUser");
                    errorCallBack.onError();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("xxx","  errorCallBack at onCancelled: sendMessageToOtherUser");
                errorCallBack.onError();
            }
        });
    }


public void observeIncomingLatestEvent(NewEventCallBack callBack) {
    if (currentUsername != null && !currentUsername.isEmpty()) {
        dbref.child(currentUsername).child(LATEST_EVENTS_FIELD_NAME)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    try {
                                        String data = dataSnapshot.getValue().toString();
                                        DataModel dataModel = gson.fromJson(data, DataModel.class);
                                        callBack.onNewEventReceived(dataModel);
                                    } catch (Exception e) {
                                        Log.d("xxx", "FirebaseClient ->observerIncomingEvent -> onDataChange " + e);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        }
                );
    } else {
        Log.d("xxx", "FirebaseClient ->observerIncomingEvent -> currentUsername is null or empty");
    }
}

}

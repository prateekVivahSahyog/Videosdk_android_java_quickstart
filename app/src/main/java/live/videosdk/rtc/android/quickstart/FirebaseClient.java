package live.videosdk.rtc.android.quickstart;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Objects;

import live.videosdk.rtc.android.quickstart.utils.DataModel;
import live.videosdk.rtc.android.quickstart.utils.ErrorCallBack;
import live.videosdk.rtc.android.quickstart.utils.NewEventCallBack;
import live.videosdk.rtc.android.quickstart.utils.SuccessCallBack;

public class FirebaseClient {

    private  final Gson gson = new Gson();
    private final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
    private String currentUsername;
    private static final String LATEST_EVENTS_FIELD_NAME = "latest_event";

    public  void login(String username, SuccessCallBack callBack){
        dbref.child(username).setValue("").addOnCompleteListener(task->{
           currentUsername = username;
           callBack.onSuccess();
        });
    }
    public  void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack){
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
//    public  void observeIncomingLatestEvent(NewEventCallBack callBack){
//
//        dbref.child(currentUsername).child(LATEST_EVENTS_FIELD_NAME)
//                .addValueEventListener(
//                        new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                try{
//                                    String data = Objects.requireNonNull(dataSnapshot.getValue()).toString();
//                                    DataModel dataModel = gson.fromJson(data,DataModel.class);
//                                    callBack.onNewEventReceived(dataModel);
//                                }catch (Exception e){
//                                    Log.d("xxx","FirebaseClient ->observerIncomingEvent -> onDataChange " + e);
//
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        }
//                );
//    }
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

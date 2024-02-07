package live.videosdk.rtc.android.quickstart.repository;

import android.util.Log;

import live.videosdk.rtc.android.quickstart.FirebaseClient;
import live.videosdk.rtc.android.quickstart.StatusCallback;
import live.videosdk.rtc.android.quickstart.utils.DataModel;
import live.videosdk.rtc.android.quickstart.utils.ErrorCallBack;
import live.videosdk.rtc.android.quickstart.utils.NewEventCallBack;
import live.videosdk.rtc.android.quickstart.utils.StatusType;
import live.videosdk.rtc.android.quickstart.utils.SuccessCallBack;

public class MainRepository {
    private final FirebaseClient firebaseClient;
    public String currentUsername;
    private void updateCurrentUsername(String username){
        this.currentUsername = username;
    }

    private MainRepository(){
        this.firebaseClient = new FirebaseClient();
    }
    private static MainRepository instance;
    public static MainRepository getInstance(){
        if(instance == null){
            instance  = new MainRepository();
        }
    return instance;
    }

    public void login(String username, SuccessCallBack callBack){
        firebaseClient.login(username, ()->{
            updateCurrentUsername(username);
            callBack.onSuccess();
        });
    }

    public void setMyStatus(StatusType currentStatus){
        firebaseClient.SetStatus(currentStatus);
    }

    public void sendCallRequest(String target,String meetingID , StatusCallback callBack
    ){
        Log.d("xxx","in main repo send call fun");

        // to others person db
        firebaseClient.sendMessageToOtherUser(
                new DataModel(target,currentUsername,meetingID, StatusType.Invited
                ), callBack

        );

    }



    public void sendResponseAccepted(String target,String sender,String meetingID ,StatusCallback callBack
    ){
        Log.d("xxx","sendResponseAccepted:->Sent call acceptance response to "+target);

        firebaseClient.sendMessageToOtherUser(
                new DataModel(target,sender,meetingID, StatusType.Accepted
                ), callBack
        );

    }

    public void sendResponseRejected(String target,String sender,String meetingID ,StatusCallback callBack){
        Log.d("xxx","sendResponseRejected:->Sent call reject response to "+target);

        firebaseClient.sendMessageToOtherUser(
                new DataModel(target,sender,meetingID, StatusType.Rejected
                ), callBack
        );
        firebaseClient.SetStatus(StatusType.Idle);
    }

    public void sendResponseEnded(String target,String sender,String meetingID ,ErrorCallBack callBack){
        Log.d("xxx","sendResponseRejected:->Sent call ended response to "+target);

        firebaseClient.sendResponseEnded(
                new DataModel(target,sender,meetingID, StatusType.Ended
                ), callBack
        );
        firebaseClient.SetStatus(StatusType.Idle);
    }

    public void sendResponseClosed(String target,ErrorCallBack callBack){
        Log.d("xxx","sendResponseRejected:->Sent call ended response to "+target);

        firebaseClient.sendResponseEnded(
                new DataModel(target,currentUsername,"", StatusType.Cancelled
                ), callBack
        );
        firebaseClient.SetStatus(StatusType.Idle);
    }

    public void subscribeForLatestEvents(NewEventCallBack callBack){
        firebaseClient.observeIncomingLatestEvent(model->{

            callBack.onNewEventReceived(model);

        });
    }
}

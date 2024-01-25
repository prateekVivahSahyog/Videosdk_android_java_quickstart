package live.videosdk.rtc.android.quickstart.repository;

import android.util.Log;

import live.videosdk.rtc.android.quickstart.FirebaseClient;
import live.videosdk.rtc.android.quickstart.utils.DataModel;
import live.videosdk.rtc.android.quickstart.utils.DataModelType;
import live.videosdk.rtc.android.quickstart.utils.ErrorCallBack;
import live.videosdk.rtc.android.quickstart.utils.NewEventCallBack;
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

    public void sendCallRequest(String target,String meetingID ,ErrorCallBack callBack){
        Log.d("xxx","in main repo send call fun");
        firebaseClient.sendMessageToOtherUser(
                new DataModel(target,currentUsername,meetingID, DataModelType.StartCall), callBack
        );
    }

    public void subscribeForLatestEvents(NewEventCallBack callBack){
        firebaseClient.observeIncomingLatestEvent(model->{
            switch (model.getType()){
                case Offer:
                    break;
                case Answer:
                    break;
                case IceCandidate:
                    break;
                case StartCall:
                    callBack.onNewEventReceived(model);
                    break;
            }
        });
    }
}

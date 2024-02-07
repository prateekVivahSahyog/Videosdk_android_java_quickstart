package live.videosdk.rtc.android.quickstart;

//public class StatusCallBack {
//}


public interface StatusCallback {
    void onUserBusy();
    void onUserOffline();
    void onError();
}

package live.videosdk.rtc.android.quickstart.utils;

import android.health.connect.datatypes.StepsCadenceRecord;

public class DataModel {
    private String target;
    private String sender;
    private String meetingID;
    private DataModelType type;

    public DataModel(String target, String sender, String meetingID, DataModelType type) {
        this.target = target;
        this.sender = sender;
        this.meetingID = meetingID;
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public DataModelType getType() {
        return type;
    }

    public void setType(DataModelType type) {
        this.type = type;
    }
}

package live.videosdk.rtc.android.quickstart.utils;

import android.health.connect.datatypes.StepsCadenceRecord;

import org.checkerframework.checker.guieffect.qual.SafeType;

public class DataModel {

    private StatusType type;

    private StatusType myStatus;

    private String  target;
    private String  sender;
    private String meetingId;

    public DataModel(String target, String sender, String meetingID,StatusType type
    ) {
        this.target = target;
        this.sender = sender;
        this.meetingId = meetingID;
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
        return meetingId;
    }

    public void setMeetingID(String meetingID) {
        this.meetingId = meetingID;
    }

    public StatusType getType() {
        return type;
    }

    public void setType(StatusType type) {
        this.type = type;
    }
}

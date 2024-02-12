package live.videosdk.rtc.android.quickstart;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import live.videosdk.rtc.android.lib.PubSubMessage;

public class MessagesAdapter  extends RecyclerView.Adapter {

    Context context;
    ArrayList<PubSubMessage> pubSubMessageArrayList;

    String RemoteName;
    String LocalName;
    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<PubSubMessage> pubSubMessageArrayList,String RemoteName,String LocalName) {
        this.context = context;
        this.pubSubMessageArrayList = pubSubMessageArrayList;
        this.RemoteName = RemoteName;
        this.LocalName = LocalName;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==ITEM_SEND){

            View view = LayoutInflater.from(context).inflate(R.layout.senderschatlayout,viewGroup,false);
            return  new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiverchatlayout,viewGroup,false);
            return  new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        PubSubMessage message = pubSubMessageArrayList.get(i);
        if(holder.getClass() == SenderViewHolder.class){

            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.textViewMessage.setText(message.getMessage());
            viewHolder.timeOfMessage.setText(convertUnixToIST(message.getTimestamp()));
        }
        else {


            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.textViewMessage.setText(message.getMessage());
            viewHolder.timeOfMessage.setText(convertUnixToIST(message.getTimestamp()));

        }


    }

    @Override
    public int getItemCount() {
       return pubSubMessageArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        PubSubMessage message =   pubSubMessageArrayList.get(position);
        if(Objects.equals(message.getSenderName(), LocalName))
            return ITEM_RECEIVE;
        else return ITEM_SEND;
    }

    class SenderViewHolder extends RecyclerView.ViewHolder{

        TextView textViewMessage;
        TextView timeOfMessage;


        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.sendermessage);
            timeOfMessage = itemView.findViewById(R.id.timeofmessage);
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder{

        TextView textViewMessage;
        TextView timeOfMessage;


        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.sendermessage);
            timeOfMessage = itemView.findViewById(R.id.timeofmessage);
        }
    }

    public static String convertUnixToIST(long epochSeconds) {
        Date date = new Date(epochSeconds);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        format.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        return format.format(date);
    }
}

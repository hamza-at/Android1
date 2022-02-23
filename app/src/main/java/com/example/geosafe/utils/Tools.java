package com.example.geosafe.utils;

import com.example.geosafe.model.User;
import com.example.geosafe.request.IFCMService;
import com.example.geosafe.request.RetrofitClient;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
    public static final String USER_INFORMATION = "userInformation";
    public static final String TOKENS ="Tokens";
    public static final String Transmitter_NAME="FromName";
    public static final String Transmitter_UID="FromUid";

    public static final String RECEIVER_UID="ToUid";

    public static final String RECEIVER_NAME="ToName";


    public static final String ACCEPTLIST ="acceptList";
    public static final String CERCLE_REQUEST="CercleRequests";
    public static final String LOCATION ="location";
    public static final String SAVED_USER_UID ="SaveUid";
    public static final String CHATS = "Chats";
    public static final String SENDER = "sender";
    public static final String RECEIVER ="receiver";
    public static final String PHOTOS = "profiles_images";
    public static User loggedUser;
    public static User UserTraced;
    public static String CHATLIST="ChatList";


    public static Date convertTimeStampsToDate(long time) {
        return new Date(new Timestamp(time).getTime());
    }


    public static String getDateFormatted(Date date) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString();

    }
    public static IFCMService getFCMService(){
        return RetrofitClient.getClient("https://fcm.googleapis.com/").create(IFCMService.class);

    }
}

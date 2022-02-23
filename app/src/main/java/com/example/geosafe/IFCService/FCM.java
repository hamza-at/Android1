package com.example.geosafe.IFCService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.geosafe.R;
import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.utils.Notifstructure;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class FCM extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.wtf("notif","notification envoyer");
        remoteMessage.getData();
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
            NotificationChannel(remoteMessage);
        else
            notification(remoteMessage);
        //ajouter au userinfo le champs request
        addRequest(remoteMessage.getData());

    }

    private void addRequest(Map<String, String> data) {
        DatabaseReference circle_request=FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION)
                .child(data.get(Tools.RECEIVER_UID))
                .child(Tools.CERCLE_REQUEST);

        User user=new User();
        user.setUid(data.get(Tools.Transmitter_UID));
        user.setEmail(data.get(Tools.Transmitter_NAME));

        circle_request.child(user.getUid()).setValue(user);
    }

    private void notification(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String title = "Circle Requests";
        String content = "New request from "+data.get(Tools.Transmitter_NAME);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder (this,"com.example.geosafe")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(),builder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void NotificationChannel(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String title = "Circle Requests";
        String content = "New request from "+data.get(Tools.Transmitter_NAME);
        Notifstructure notifstructure;
        Notification.Builder builder;
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notifstructure = new Notifstructure(this);
        builder = notifstructure.getRTNotification(title,content,defaultSound);

        notifstructure.getManager().notify(new Random().nextInt(),builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null)
        {
            final DatabaseReference tokens = FirebaseDatabase.getInstance()
                    .getReference(Tools.TOKENS);
           tokens.child(user.getUid()).setValue(s);
        }
    }
}
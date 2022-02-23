package com.example.geosafe.IFCService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.example.geosafe.utils.Tools;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import io.paperdb.Paper;

public class LocalisationReceiver extends BroadcastReceiver {
    public static final String ACTION="com.example.geosafe.UPDATE_LOCALISATION";
    DatabaseReference Localisation;
    String Uid;

    public LocalisationReceiver() {
        Localisation = FirebaseDatabase.getInstance().getReference(Tools.LOCATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Paper.init(context);

        Uid = Paper.book().read(Tools.SAVED_USER_UID);
        Log.wtf("uid from paper de loggeduser",Uid);
        if (intent !=null){
            final String action = intent.getAction();
            if (action.equals(ACTION)){
                LocationResult result = LocationResult.extractResult(intent);
                if (result !=null)
                { Location location=result.getLastLocation();
                    if (Tools.loggedUser !=null)//l'application tourne
                    {
                        Localisation.child(Tools.loggedUser.getUid()).setValue(location);
                    }
                    else{//app destroyed est le service marche en arriere plan
                        Localisation.child(Uid).setValue(location);
                    }
                }
            }
        }


    }
}

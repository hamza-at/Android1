package com.example.geosafe.request;


import android.app.DownloadManager;

import com.example.geosafe.model.MyResponse;
import com.example.geosafe.model.Request;

import retrofit2.http.Headers;
import retrofit2.http.POST;
import io.reactivex.Observable;
import retrofit2.http.Body;

public interface IFCMService {
    @Headers({
  "Content-Type:application/json",
     "Authorization:key=AAAAXUMUIzQ:APA91bFaHqlqYOknrZVn6fxXVoVzD9fQ6gOawEh3_bkAnuBpUpYCILQCR7HhhhYN12O_gWI-mWZqSInmPU052zmGI3_LjlgejxDRFic91YnKBQ_VnLpb5W_0SWGIQeDkFoKJjWo4WTOi"

    })
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser (@Body Request body);

}

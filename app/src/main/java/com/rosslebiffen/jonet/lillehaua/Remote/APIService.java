package com.rosslebiffen.jonet.lillehaua.Remote;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


import com.rosslebiffen.jonet.lillehaua.Model.DataMessage;
import com.rosslebiffen.jonet.lillehaua.Model.MyResponse;


/**
 * Created by Jone on 03.03.2018.
 */

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAWhUIK64:APA91bE77xFs8mmiU5pKc9FM9JK0arHWRGpwetrq69AEx1Z9Q4a7cVSVu7P1Wz2pzdIY65u-ITf_AGLCkAO5R8-eUPS2pPHKW67MwxkM0PbQK4n34SFolue89IhGNd_gXQEcf7JMIsK_"
            }

    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);


}

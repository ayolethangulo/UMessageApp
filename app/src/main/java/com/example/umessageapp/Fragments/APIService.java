package com.example.umessageapp.Fragments;

import com.example.umessageapp.Notifications.MyResponse;
import com.example.umessageapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA6Yizw-k:APA91bHupVL2rzn8CJ_tT65mHZJakrQ4AJviSDO9-6hpI1I09vH1YrAw9D8rXEZQC7BwJ-HLfZOJhwlxLRdMKddHbnAhOyMxjJYor2palxipGqExdF8uf2ejn39_0SZfsYoWctsY0CXb"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

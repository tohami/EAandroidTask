package com.example.tohami.eaandroidtask.system;


import com.example.tohami.eaandroidtask.pojos.CarsOnlineResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Restapi {

    @GET("carsonline")
    Call<CarsOnlineResponse> getCarsOnline(@Query("Ticks") String ticks);

    }
package com.madalin.licenta;

import com.madalin.licenta.model.Melodie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MadalinApi {
    @GET("music")
    Call<List<Melodie>> getMelodii();
}

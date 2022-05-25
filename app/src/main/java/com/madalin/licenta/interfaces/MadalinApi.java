package com.madalin.licenta.interfaces;

import com.madalin.licenta.models.Melodie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MadalinApi {
    @GET("music")
    Call<List<Melodie>> getMelodii();
}

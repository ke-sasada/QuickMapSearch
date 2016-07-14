package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.PlaceResponce;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * DirectionAPI利用のためのインターフェース
 * Created by kengo on 16/07/10.
 */
public interface DirectionApiService {
    @Headers("Accept-Language: ja")
    @GET("/maps/api/directions/json")
    Call<DirectionResponce> requestPlaces(@Query("mode") String mode,
                                          @Query("origin") String types,
                                          @Query("destination") String location,
                                          @Query("waypoints") String waypoints,
                                          @Query("key") String key);
}

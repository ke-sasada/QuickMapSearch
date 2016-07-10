package jp.ac.titech.itpro.sdl.quickmapsearch.placeapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by kengo on 16/07/09.
 */
public interface PlaceApiService {
    @Headers("Accept-Language: ja")
    @GET("/maps/api/place/nearbysearch/json")
    Call<PlaceResponce> requestPlaces(@Query("types") String types,
                                      @Query("location") String location,
                                      @Query("radius") String radius,
                                      @Query("sensor") String sensor,
                                      @Query("key") String key);
}

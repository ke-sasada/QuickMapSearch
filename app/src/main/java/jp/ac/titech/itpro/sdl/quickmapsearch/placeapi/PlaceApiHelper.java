package jp.ac.titech.itpro.sdl.quickmapsearch.placeapi;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import jp.ac.titech.itpro.sdl.quickmapsearch.R;
import jp.ac.titech.itpro.sdl.quickmapsearch.SearchItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kengo on 16/07/09.
 */
public class PlaceApiHelper {
    private static final String TAG = PlaceApiHelper.class.getSimpleName();

    private Context context;

    public PlaceApiHelper(Context context){
        this.context = context;
    }

    public void requestPlaces(String types, SearchItem.SEARCH_TYPE search_type, LatLng latLng, int radius, Callback<PlaceResponce> callback){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.places_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PlaceApiService service = retrofit.create(PlaceApiService.class);
        if(search_type.equals(SearchItem.SEARCH_TYPE.GENRE)) {
            Call<PlaceResponce> call = service.requestPlaces(types,
                    String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude),
                    String.valueOf(radius),
                    "false",
                    "",
                    context.getString(R.string.api_google_maps_key_browser));
            call.enqueue(callback);
        }else{
            Call<PlaceResponce> call = service.requestPlaces("",
                    String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude),
                    String.valueOf(radius),
                    "false",
                    types,
                    context.getString(R.string.api_google_maps_key_browser));
            call.enqueue(callback);
        }
        Log.d(TAG,"requestPlaces ended");
    }
}

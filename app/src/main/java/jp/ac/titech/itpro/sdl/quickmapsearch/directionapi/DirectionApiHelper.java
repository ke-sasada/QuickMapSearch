package jp.ac.titech.itpro.sdl.quickmapsearch.directionapi;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.util.LinkedList;

import jp.ac.titech.itpro.sdl.quickmapsearch.R;
import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.PlaceResponce;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kengo on 16/07/10.
 */
public class DirectionApiHelper {
    private static final String TAG = DirectionApiHelper.class.getSimpleName();

    private Context context;

    public DirectionApiHelper(Context context){
        this.context = context;
    }

    public void requestPlaces(LinkedList<Marker> pointList, Callback<DirectionResponce> callback){
        if(pointList == null || pointList.size() < 2){
            Log.d(TAG,"cannot search : size < 2");
            return ;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.places_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionApiService service = retrofit.create(DirectionApiService.class);

        Marker m1 = pointList.get(0);
        Marker m2 = pointList.get(1);

        Call<DirectionResponce> call = service.requestPlaces(
                String.valueOf(m1.getPosition().latitude) + "," + String.valueOf(m1.getPosition().longitude),
                String.valueOf(m2.getPosition().latitude) + "," + String.valueOf(m2.getPosition().longitude),
                context.getString(R.string.api_google_maps_key_browser));
        call.enqueue(callback);

        Log.d(TAG,"requestPlaces ended");
    }
}

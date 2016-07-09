package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private android.location.Location currentLocation;
    private LatLng currentLatLng;
    PlaceApiHelper helper;

    private enum UpdatingState {STOPPED, REQUESTING, STARTED}
    private UpdatingState state = UpdatingState.STOPPED;
    private List<ResultList> allResult;


    private GoogleMap googleMap;
    private MapFragment mapFragment;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private Button searchButton;
    private Button spinnerButton;
    private ArrayAdapter<String> spinnerAdapter;

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final static int REQCODE_PERMISSIONS = 1111;

    private SearchItemList itemList;

    private int count;
    private int maxItemSize;

    private Map<String,Bitmap> iconMap;
    private int buttonSelectedIndex = 0;
    private AlertDialog buttonAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        helper = new PlaceApiHelper(this);

        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(onClickListener);

        allResult = new ArrayList<ResultList>();
        currentLatLng = new LatLng(0,0);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        itemList = new SearchItemList("test");
        SearchItem item = new SearchItem(SearchItem.SEARCH_TYPE.GENRE,"school",BitmapDescriptorFactory.HUE_GREEN);
        itemList.addItem(item);
        item = new SearchItem(SearchItem.SEARCH_TYPE.GENRE,"toilet", BitmapDescriptorFactory.HUE_AZURE);
        itemList.addItem(item);
        item = new SearchItem(SearchItem.SEARCH_TYPE.GENRE,"food", BitmapDescriptorFactory.HUE_RED);
        itemList.addItem(item);

        iconMap = new HashMap<String, Bitmap>();

        spinnerButton = (Button)findViewById(R.id.spinner_button);
        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice);
        spinnerAdapter.addAll(mkDataList(10));
        spinnerButton.setOnClickListener(onClickListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (state != UpdatingState.STARTED && googleApiClient.isConnected())
            startLocationUpdate(true);
        else
            state = UpdatingState.REQUESTING;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (state == UpdatingState.STARTED)
            stopLocationUpdate();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"onMapReady");
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (state == UpdatingState.REQUESTING) {
            startLocationUpdate(true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspented");
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        currentLocation = location;
        currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onRequestPermissionsResult(int reqCode,
                                           @NonNull String[] permissions, @NonNull int[] grants) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (reqCode) {
            case REQCODE_PERMISSIONS:
                startLocationUpdate(false);
                break;
        }
    }

    private void startLocationUpdate(boolean reqPermission) {
        Log.d(TAG, "startLocationUpdate: " + reqPermission);
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (reqPermission)
                    ActivityCompat.requestPermissions(this, PERMISSIONS, REQCODE_PERMISSIONS);
                else
                    Toast.makeText(this, getString(R.string.toast_requires_permission, permission),
                            Toast.LENGTH_SHORT).show();
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        state = UpdatingState.STARTED;
    }

    private void stopLocationUpdate() {
        Log.d(TAG, "stopLocationUpdate");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        state = UpdatingState.STOPPED;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");
            switch(view.getId()){
                case R.id.search_button:
                    allResult.clear();

                    maxItemSize = itemList.getItemList().size();
                    count = 0;
                    for (SearchItem item : itemList.getItemList()) {
                        {
                            helper.requestPlaces(item.getWord(), currentLatLng, 500, resultCallBack);
                        }
                    }
                    break;
                case R.id.spinner_button:
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setTitle("テスト");
                    builder.setSingleChoiceItems(spinnerAdapter, buttonSelectedIndex,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    buttonSelectedIndex = i;
                                    String item = spinnerAdapter.getItem(i);
                                    spinnerButton.setText(item);
                                }
                            });

                    builder.setPositiveButton("OK",null);
                    buttonAlertDialog = builder.create();
                    buttonAlertDialog.show();

            }
            Log.d(TAG,"onClickended");
        }
    };

    private Callback<Response> resultCallBack = new Callback<Response>(){
        @Override
        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
            Log.d(TAG,"onResponse");

            googleMap.clear();
            ResultList results = new ResultList(response.body().getResults(),itemList.getItemList().get(count).getMarkerOptions());

            Iterator itr = results.getResultList().iterator();

            while(itr.hasNext()){
                Result result = (Result)itr.next();
                boolean isExist = false;

                for(ResultList rList:allResult){
                    for(Result r:rList.getResultList()){
                        if(result.getName().equals(r.getName())) {
                            isExist = true;
                            break;
                        }
                    }
                    if(isExist){
                        break;
                    }
                }
                if(isExist){
                    itr.remove();
                }
            }

            allResult.add(results);

            count++;
            if(count == maxItemSize){
                for(ResultList resultList: allResult){
                    for(Result r:resultList.getResultList()) {
                        Log.d(TAG,r.getName());
                        Location location = r.getGeometry().getLocation();
                        LatLng latLng = new LatLng(location.getLat(), location.getLng());
                        String name = r.getName();
                        final String iconURL = r.getIcon();
                        if(!iconMap.containsKey(iconURL)){
                            Thread thread = new Thread(new Runnable(){
                                @Override
                                public void run(){
                                    URL url ;
                                    try {
                                        url = new URL(iconURL);
                                        Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                        iconMap.put(iconURL,bmp);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG,"endBitMapThread");
                                }
                            });
                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG,"endMainThread");
                        }
                        if(iconMap.containsKey(iconURL)) {
                            googleMap.addMarker(resultList.getMarkerOptions().position(latLng).title(name)
                                    .icon(BitmapDescriptorFactory.fromBitmap(iconMap.get(iconURL))));
                        }
                    }
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                Log.d(TAG,"viewMarker");
            }

            Log.d(TAG,"callback ended");
        }

        @Override
        public void onFailure(Call<Response> call, Throwable t) {
            t.printStackTrace();
        }


    };

    private List<String> mkDataList(int cnt){
        List<String> list = new ArrayList<>();
        for(int i = 0; i < cnt; i++){
            list.add(String.valueOf(i));
        }
        return list;
    }


}




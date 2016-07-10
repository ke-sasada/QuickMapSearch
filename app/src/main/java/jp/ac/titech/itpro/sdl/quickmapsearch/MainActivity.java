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
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import jp.ac.titech.itpro.sdl.quickmapsearch.directionapi.DirectionApiHelper;
import jp.ac.titech.itpro.sdl.quickmapsearch.directionapi.DirectionResponce;
import jp.ac.titech.itpro.sdl.quickmapsearch.directionapi.Leg;
import jp.ac.titech.itpro.sdl.quickmapsearch.directionapi.RootResult;
import jp.ac.titech.itpro.sdl.quickmapsearch.directionapi.Step;
import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.Location;
import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.PlaceApiHelper;
import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.PlaceResponce;
import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.PlaceResult;
import jp.ac.titech.itpro.sdl.quickmapsearch.placeapi.ResultList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MAX_WAYPOINTS = 1;

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private android.location.Location currentLocation;
    private LatLng currentLatLng;
    PlaceApiHelper placeHelper;
    DirectionApiHelper directionHelper;

    private enum UpdatingState {STOPPED, REQUESTING, STARTED}

    private UpdatingState state = UpdatingState.STOPPED;
    private List<ResultList> allResult;


    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private MapFragment mapFragment;
    private LocationRequest locationRequest;

    private Button searchButton;
    private Button spinnerButton;
    private Button debug_FileDeleteButton;
    private Button debug_naviTestButton;
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayAdapter<String> genreAdapter;
   // private RootAdapter rootAdapter;

    private final static int REQCODE_PERMISSIONS = 1111;
    private int count;
    private int maxItemSize;
    private int buttonSelectedIndex = 0;

    private HashMap<String, Bitmap> iconMap;

    private AlertDialog selectListDialog;
    private AlertDialog addListDialog;
    private LinkedHashMap<String, SearchItemList> itemListHashMap;

    private SearchItemList selectedList;

    private HashMap<String,PlaceResult> makerOptionsMap;
    private LinkedList<Marker> rootList;
    private Marker currentPositionMarker = null;

    private Polyline rootLine = null;



    //private RecyclerView recyclerView;
    //private ArrayList<String> dataList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            FileInputStream fis = openFileInput("BookMark.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            itemListHashMap = (LinkedHashMap<String, SearchItemList>) ois.readObject();
            Log.d(TAG, "loading 'BookMark.dat' completed");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not Found. Start no file.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        placeHelper = new PlaceApiHelper(this);
        directionHelper = new DirectionApiHelper(this);

        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(onClickListener);
        debug_FileDeleteButton = (Button) findViewById((R.id.debug_file_delete));
        debug_FileDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d(TAG, "delete file");
                    deleteFile("BookMark.dat");
                    Toast.makeText(MainActivity.this, "Debug:Delete 'BookMark.dat' completed", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        allResult = new ArrayList<ResultList>();
        currentLatLng = null;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        iconMap = new HashMap<String, Bitmap>();

        spinnerButton = (Button) findViewById(R.id.spinner_button);
        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice);
        spinnerButton.setOnClickListener(onClickListener);
        genreAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice);

        makerOptionsMap = new HashMap<String,PlaceResult>();
        rootList = new LinkedList<Marker>();

        //recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        //recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //dataList.add("rootView");
        //rootAdapter = new RootAdapter(this,dataList);
        //recyclerView.setAdapter(rootAdapter);

        debug_naviTestButton = (Button)findViewById(R.id.navitest);
        debug_naviTestButton.setOnClickListener(onClickListener);

        // test data
        if (itemListHashMap == null || itemListHashMap.size() == 0) {
            itemListHashMap = new LinkedHashMap<String, SearchItemList>();
            SearchItem s = new SearchItem(SearchItem.SEARCH_TYPE.GENRE, "toilet", 0.0f);
            ArrayList<SearchItem> sl = new ArrayList<SearchItem>();
            sl.add(s);
            SearchItemList l = new SearchItemList(0, "Test", sl);
            itemListHashMap.put(l.getName(), l);
            s = new SearchItem(SearchItem.SEARCH_TYPE.GENRE, "school", 0.0f);
            sl = new ArrayList<SearchItem>();
            sl.add(s);
            l = new SearchItemList(1, "Test2", sl);
            itemListHashMap.put(l.getName(), l);
            //
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;
        googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                if(!rootList.isEmpty() && rootList.getLast().equals(marker)){
                    Toast.makeText(MainActivity.this,"同じ地点は連続して登録出来ません",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,marker.getTitle() + "が登録されました",Toast.LENGTH_SHORT).show();
                    rootList.clear();
                    rootList.addLast(currentPositionMarker);
                    rootList.addLast(marker);
                    directionHelper.requestPlaces(rootList,directionResponceCallback);
                   // dataList.add(marker.getTitle());
                   // rootAdapter.notifyDataSetChanged();
                }
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


    static boolean firstAnimated = false;
    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        currentLocation = location;
        currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        if(!firstAnimated){
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            firstAnimated = true;
            currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("現在位置"));
        }
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

        private void createListDialog(){

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this);
            builder.setTitle("ブックマーク");

            builder.setPositiveButton("OK",null);
            builder.setNeutralButton("+",null);
            spinnerAdapter.clear();
            for(SearchItemList list:itemListHashMap.values()) {
                spinnerAdapter.add(list.getName());
            }

            builder.setSingleChoiceItems(spinnerAdapter, buttonSelectedIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    buttonSelectedIndex = i;
                }
            });

            selectListDialog = builder.create();

            selectListDialog.show();
            Button addButton = selectListDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemListHashMap.size() < 10) {
                        createAddListDialog();
                    }else{
                        Toast.makeText(getApplicationContext(),"Up to 10",Toast.LENGTH_LONG).show();
                    }
                }
            });
            Button okButton = selectListDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedList = itemListHashMap.get(spinnerAdapter.getItem(buttonSelectedIndex));
                    spinnerButton.setText(selectedList.getName());
                    try{
                        FileOutputStream fos = openFileOutput("BookMark.dat",MODE_PRIVATE);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(itemListHashMap);
                        oos.close();
                        Log.d(TAG,"saving 'BookMark.dat' completed");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    selectListDialog.dismiss();
                }
            });
        }

        private void createAddListDialog() {

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this);
            builder.setTitle("追加");
            builder.setPositiveButton("OK", null);
            builder.setNegativeButton("Cancel",null);
            final String[] genreList = getResources().getStringArray(R.array.genre_list);
            final ArrayList<Integer> checkedItems = new ArrayList<Integer>();
            builder.setMultiChoiceItems(genreList, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    if(b)checkedItems.add(i);
                    else checkedItems.remove((Integer)i);
                }
            });
            addListDialog = builder.create();
            addListDialog.show();

            Button okButton = addListDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SearchItemList list = new SearchItemList(itemListHashMap.size()+1,String.valueOf(itemListHashMap.size()+1));
                    for(int i : checkedItems){
                        if(i < genreList.length) {
                            SearchItem item = new SearchItem(SearchItem.SEARCH_TYPE.GENRE,genreList[i],0.0f);
                            list.addItem(item);
                        }
                    }
                    if(list.getItemList().size() == 0){
                        Toast.makeText(MainActivity.this,"no selected",Toast.LENGTH_LONG).show();
                    }else {
                        itemListHashMap.put(list.getName(), list);
                        spinnerAdapter.add(list.getName());
                        addListDialog.dismiss();
                        Log.d(TAG, "endaddButtonClick");
                    }
                }
            });

        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");
            switch(view.getId()){
                case R.id.search_button:
                    allResult.clear();
                    if(selectedList != null) {
                        maxItemSize = selectedList.getItemList().size();
                        count = 0;
                        for (SearchItem item : selectedList.getItemList()) {
                            {
                                placeHelper.requestPlaces(item.getWord(), currentLatLng, 500, placeResponceCallBack);
                            }
                        }
                    }else{
                        Log.d(TAG,"not selected");
                    }
                    break;
                case R.id.spinner_button:
                    createListDialog();
                    break;
                case R.id.navitest:
                    directionHelper.requestPlaces(null,directionResponceCallback);
                    Log.d(TAG,"navigate test");
                    break;

            }
            Log.d(TAG,"onClick_ended");
        }
    };

    private Callback<DirectionResponce> directionResponceCallback = new Callback<DirectionResponce>(){

        @Override
        public void onResponse(Call<DirectionResponce> call, Response<DirectionResponce> response) {
            if(response.isSuccessful()){
                Log.d(TAG,"DirectionResponce");
            }
            if(rootLine != null){rootLine.remove();}
            List<String> encodedPolyLine = new ArrayList<>();
            List<RootResult> rootResultList = response.body().getResults();

            if(rootResultList != null ){
                for(RootResult r:rootResultList){
                    for(Leg l: r.getLegs()){
                        for(Step s:l.getSteps()){
                            encodedPolyLine.add(s.getPolyline().getPoints());
                        }
                    }
                }
            }

            List<LatLng> decodedPolyLine = new ArrayList<LatLng>();
            for(String s:encodedPolyLine) {
               decodedPolyLine.addAll(PolyUtil.decode(s));
            }

           // for(LatLng l:decodedPolyLine){
           //     Log.d(TAG,String.valueOf(l.latitude) +"," + String .valueOf(l.longitude));
           // }

            PolylineOptions po = new PolylineOptions();
            po.addAll(decodedPolyLine);

            rootLine = googleMap.addPolyline(po);

        }

        @Override
        public void onFailure(Call<DirectionResponce> call, Throwable t) {

        }
    };

    private Callback<PlaceResponce> placeResponceCallBack = new Callback<PlaceResponce>(){
        @Override
        public void onResponse(Call<PlaceResponce> call, retrofit2.Response<PlaceResponce> response) {
            Log.d(TAG,"onResponse");

            googleMap.clear();
            MarkerOptions mo = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(selectedList.getItemList().get(count).getColor()));
            ResultList results = new ResultList(response.body().getResults(),mo);

            Iterator itr = results.getPlaceResultList().iterator();

            while(itr.hasNext()){
                PlaceResult placeResult = (PlaceResult)itr.next();
                boolean isExist = false;

                for(ResultList rList:allResult){
                    for(PlaceResult r:rList.getPlaceResultList()){
                        if(placeResult.getName().equals(r.getName())) {
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
                    for(PlaceResult r:resultList.getPlaceResultList()) {
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
                            Marker m = googleMap.addMarker(resultList.getMarkerOptions().position(latLng).title(name)
                                    .icon(BitmapDescriptorFactory.fromBitmap(iconMap.get(iconURL))));
                            r.setMarkerOptions(resultList.getMarkerOptions());
                            makerOptionsMap.put(m.getId(),r);
                        }
                    }
                }
                currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("現在位置"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

                Log.d(TAG,"viewMarker");
            }
            Log.d(TAG,"callback ended");
        }
        @Override
        public void onFailure(Call<PlaceResponce> call, Throwable t) {
            t.printStackTrace();
        }
    };

}




package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
    private static final int MAX_SEARCHWORD = 3;
    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };


    private enum UpdatingState {STOPPED, REQUESTING, STARTED}
    private UpdatingState state = UpdatingState.STOPPED;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private MapFragment mapFragment;
    private LocationRequest locationRequest;
    private android.location.Location currentLocation;
    private LatLng currentLatLng;
    private Marker currentPositionMarker = null;
    PlaceApiHelper placeHelper;
    DirectionApiHelper directionHelper;

    private FloatingActionButton searchButton;
    private FloatingActionButton spinnerButton;
    private FloatingActionButton open_RootListButton;
    private FloatingActionButton close_RootListButton;
    private FloatingActionButton debug_naviTestButton;
    private FloatingActionButton startNaviButton;
    private FloatingActionButton endNaviButton;

    private BookmarkListView bookmarkListView;
    private AddBookmarkListView addListView;
    private RootListView rootListView;
    private View addListDialogView;

    private BookMarkAdapter bookmarkAdapter;
    private AddListAdapter addListAdapter;

    private AlertDialog bookmarkListDialog;
    private AlertDialog addListDialog;

    private final static int REQCODE_PERMISSIONS = 1111;
    private int count;
    private int maxItemSize;
    private Boolean isShowRootList;

    private SearchItemList selectedList;
    private Polyline rootLine = null;

    private HashMap<String,PlaceResult> makerOptionsMap;
    private LinkedHashMap<String, SearchItemList> itemListHashMap;
    private LinkedList<Marker> rootList;
    private List<ResultList> allResult;
    private HashMap<String,String> iconMap;
    private SampleAdapter rootAdapter;
    private int draggingPosition;


    static boolean firstAnimated = false;
    boolean isNaviStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bookmarkAdapter = new BookMarkAdapter(this,R.layout.bookmark_itemlayout,R.id.bookmark_text);
        bookmarkListView = new BookmarkListView(this);
        addListAdapter = new AddListAdapter(this,R.layout.bookmark_itemlayout,R.id.bookmark_text);

        try {
            FileInputStream fis = openFileInput("BookMark.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            itemListHashMap = (LinkedHashMap<String, SearchItemList>) ois.readObject();
            for(SearchItemList list:itemListHashMap.values()) {
                bookmarkAdapter.add(list.getName());
            }
            if(itemListHashMap.size() > 0){
                bookmarkListView.setSelectNo(0);
                bookmarkListView.setOnItemClickListener(bookmarkListView);
            }
            bookmarkListView.setAdapter(bookmarkAdapter);

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

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        debug_naviTestButton = (FloatingActionButton) findViewById(R.id.delete_navigation);
        debug_naviTestButton.setOnClickListener(onClickListener);
        searchButton = (FloatingActionButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(onClickListener);
        spinnerButton = (FloatingActionButton) findViewById(R.id.spinner_button);
        spinnerButton.setOnClickListener(onClickListener);
        open_RootListButton = (FloatingActionButton)findViewById(R.id.open_rootbutton);
        open_RootListButton.setOnClickListener(onClickListener);
        close_RootListButton = (FloatingActionButton)findViewById(R.id.close_rootbutton);
        close_RootListButton.setOnClickListener(onClickListener);
        startNaviButton = (FloatingActionButton)findViewById(R.id.start_navi);
        endNaviButton = (FloatingActionButton)findViewById(R.id.end_navi);
        startNaviButton.setOnClickListener(onClickListener);
        endNaviButton.setOnClickListener(onClickListener);

        makerOptionsMap = new HashMap<String,PlaceResult>();
        rootList = new LinkedList<Marker>();
        allResult = new ArrayList<ResultList>();
        iconMap = new HashMap<String, String>();

        rootListView = (RootListView)findViewById(R.id.root_listview);

        rootAdapter = new SampleAdapter();
        rootListView.setOnItemClickListener(rootListView);
        rootListView.setDragListener(new DragListener());
        rootListView.setSortable(true);
        rootListView.setAdapter(rootAdapter);

        String[] genre = getResources().getStringArray(R.array.genre_list);
        String[] icon = getResources().getStringArray(R.array.genre_pic);
        for(int i = 0; i < genre.length;i++){
            iconMap.put(genre[i],icon[i]);
        }

        isShowRootList = false;
        currentLatLng = new LatLng(35.681368,139.766076);
    }

    class SampleAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return rootList.size();
        }

        @Override
        public String getItem(int position) {
            return rootList.get(position).getTitle();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void removeItem(int position){
            rootList.remove(position);
            startDirectionRequest();
            rootAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        android.R.layout.simple_list_item_1, null);
            }
            final TextView view = (TextView) convertView;
            view.setText(rootList.get(position).getTitle());
            view.setVisibility(position == draggingPosition ? View.INVISIBLE
                    : View.VISIBLE);
            return convertView;
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

    public void startDirectionRequest(){
        directionHelper.requestPlaces(rootList,directionResponceCallback);
    }

    /**
     * マップ利用の準備が終わった後に行う動作
     * @param googleMap
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        this.googleMap = googleMap;
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                if(!rootList.isEmpty() && rootList.getLast().equals(marker)){
                    Toast.makeText(MainActivity.this,"同じ地点は連続して登録出来ません",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,marker.getTitle() + "が登録されました",Toast.LENGTH_SHORT).show();
                    rootList.set(rootList.size()-1,marker);
                    rootList.add(currentPositionMarker);
                    startDirectionRequest();
                    rootAdapter.notifyDataSetChanged();
                }
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_root);
                Snackbar.make(layout,marker.getTitle(),Snackbar.LENGTH_INDEFINITE).show();
                return false;
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

    /**
     * 現在地変更の際の動作
     * @param location
     */
    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        currentLocation = location;
        currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        if(!firstAnimated){
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            firstAnimated = true;
            currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("現在位置").flat(true));
            rootList.add(currentPositionMarker);
            rootList.add(currentPositionMarker);
        }else {
            if(isNaviStarted){

            }
            rootList.set(0, currentPositionMarker);
            rootList.set(rootList.size() - 1, currentPositionMarker);
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

    /**
     * MainActivity内の主なボタンに対するリスナー
     *
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        /**
         * ブックマーク表示ダイアログの生成
         */
        private void createListDialog(){
            if(bookmarkListDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle("ブックマーク");

                builder.setPositiveButton("OK", null);
                builder.setNeutralButton("+", null);
                builder.setView(bookmarkListView);
                bookmarkListDialog = builder.create();

                bookmarkListDialog.show();
                Button addButton = bookmarkListDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (itemListHashMap.size() < 10) {
                            createAddListDialog();
                        } else {
                            Toast.makeText(getApplicationContext(), "Up to 10", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Button okButton = bookmarkListDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(bookmarkListView.getSelectView() != null){
                            TextView text = (TextView)bookmarkListView.getSelectView().findViewById(R.id.bookmark_text);
                            selectedList = itemListHashMap.get(text.getText());

                        }
                        try {
                            FileOutputStream fos = openFileOutput("BookMark.dat", MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(itemListHashMap);
                            oos.close();
                            Log.d(TAG, "saving 'BookMark.dat' completed");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bookmarkListDialog.dismiss();
                    }
                });
            }else{
                bookmarkListDialog.show();
            }
        }

        /**
         * ブックマーク生成ダイアログの作成
         */
        private void createAddListDialog() {
            EditText title;
            final String[] genreList = getResources().getStringArray(R.array.genre_list);

            if(addListDialog == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                addListDialogView = inflater.inflate(R.layout.addbookmark_layout,(ViewGroup)findViewById(R.id.addlayout_root));
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle("ブックマークの追加");
                builder.setPositiveButton("OK", null);
                builder.setNegativeButton("Cancel", null);
                builder.setNeutralButton("+",null);
                builder.setView(addListDialogView);
                addListView = (AddBookmarkListView)addListDialogView.findViewById(R.id.addlist);
                addListAdapter.addAll(genreList);
                addListView.setAdapter(addListAdapter);
                addListView.setOnItemClickListener(addListView);
                addListDialog = builder.create();
                title = (EditText)addListDialogView.findViewById(R.id.titletext);
                title.setText("ブックマーク"+String.valueOf(itemListHashMap.size()));
                addListDialog.show();
                final EditText addWord = (EditText)addListDialog.findViewById(R.id.addWord);
                addWord.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        if((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)){
                            InputMethodManager inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(addWord.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                            return true;
                        }
                        return false;                    }
                });

                Button okButton = addListDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText title;
                        title = (EditText)addListDialogView.findViewById(R.id.titletext);
                        if(!title.getText().toString().equals("")) {
                            SearchItemList list = new SearchItemList(itemListHashMap.size() + 1, title.getText().toString());
                            for (int i : addListView.getCheckedNo()) {
                                if (i < genreList.length) {
                                    SearchItem item = new SearchItem(SearchItem.SEARCH_TYPE.GENRE, genreList[i], 0.0f);
                                    list.addItem(item);
                                }else{
                                    SearchItem item = new SearchItem(SearchItem.SEARCH_TYPE.WORD,addListAdapter.getItem(i),0.0f);
                                    list.addItem(item);
                                }
                            }

                            if (list.getItemList().size() == 0) {
                                Toast.makeText(MainActivity.this, "no selected", Toast.LENGTH_LONG).show();
                            } else {
                                itemListHashMap.put(list.getName(), list);
                                bookmarkAdapter.add(list.getName());
                                bookmarkAdapter.notifyDataSetChanged();
                                addListDialog.dismiss();
                                Log.d(TAG, "endaddButtonClick");
                            }
                        }else{
                            Toast.makeText(MainActivity.this,"No Title",Toast.LENGTH_SHORT);
                        }
                     }
                });
                Button nButton = addListDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                nButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText addWord = (EditText)addListDialogView.findViewById(R.id.addWord);
                        if(!addWord.getText().toString().equals("") && addListAdapter.getCount() - genreList.length < MAX_SEARCHWORD){
                            addListAdapter.add(addWord.getText().toString());
                            addListAdapter.notifyDataSetChanged();
                        }
                    }
                });

            }else{

                title = (EditText)addListDialogView.findViewById(R.id.titletext);
                title.setText("ブックマーク"+String.valueOf(itemListHashMap.size()));
                addListView.clearChecked();
                addListAdapter.clear();
                addListAdapter.addAll(genreList);
                addListAdapter.notifyDataSetChanged();
                addListDialog.show();
            }

        }

        /**
         * ボタン動作の定義
         * @param view
         */
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
                                placeHelper.requestPlaces(item.getWord(), item.getSearch_type(), currentLatLng, 500, placeResponceCallBack);
                            }
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"現在地のみ表示します",Toast.LENGTH_SHORT);
                        Log.d(TAG,"not selected");
                    }
                    break;
                case R.id.spinner_button:
                    createListDialog();
                    break;
                case R.id.delete_navigation:
                    if(rootLine != null) {
                        rootLine.remove();
                        rootList.clear();
                        rootList.add(currentPositionMarker);
                        rootList.add(currentPositionMarker);
                        rootAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,"ルート情報の削除",Toast.LENGTH_SHORT);
                        Log.d(TAG, "delete PolyLine List");
                    }
                    break;
                case R.id.open_rootbutton:
                    if(!isShowRootList) isShowRootList = true;
                    open_RootListButton.setVisibility(View.GONE);
                    close_RootListButton.setVisibility(View.VISIBLE);
                    rootListView.setVisibility(View.VISIBLE);
                    break;
                case R.id.close_rootbutton:
                    if(isShowRootList) isShowRootList = false;
                    open_RootListButton.setVisibility(View.VISIBLE);
                    close_RootListButton.setVisibility(View.GONE);
                    rootListView.setVisibility(View.GONE);
                    break;
                case R.id.start_navi:
                    startNaviButton.setVisibility(View.GONE);
                    endNaviButton.setVisibility(View.VISIBLE);
                    isNaviStarted = false;
                    break;
                case R.id.end_navi:
                    startNaviButton.setVisibility(View.VISIBLE);
                    endNaviButton.setVisibility(View.GONE);
                    isNaviStarted = true;

            }
            Log.d(TAG,"onClick_ended");
        }
    };

    /**
     * directionAPIの結果を解析、表示
     */
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
            PolylineOptions po = new PolylineOptions();
            po.color(Color.BLUE);
            po.addAll(decodedPolyLine);
            rootLine = googleMap.addPolyline(po);

        }
        @Override
        public void onFailure(Call<DirectionResponce> call, Throwable t) {

        }
    };

    /**
     * placeAPIの結果を解析、表示
     */
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
                placeResult.setSearchWord(selectedList.getItemList().get(count).getWord());
            }
            allResult.add(results);
            count++;
            if(count == maxItemSize){
                for(ResultList resultList: allResult){
                    for(PlaceResult r:resultList.getPlaceResultList()) {
                        Location location = r.getGeometry().getLocation();
                        LatLng latLng = new LatLng(location.getLat(), location.getLng());
                        String name = r.getName();
                        Marker m;
                        if(iconMap.containsKey(r.getSearchWord())) {
                            String iconName = iconMap.get(r.getSearchWord());
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(getResources().getIdentifier(iconName, "drawable", "jp.ac.titech.itpro.sdl.quickmapsearch"));
                            m = googleMap.addMarker(resultList.getMarkerOptions().position(latLng).title(name)
                                    .icon(icon));
                        }else{
                            m = googleMap.addMarker(resultList.getMarkerOptions().position(latLng).title(name)
                                    );
                        }
                        r.setMarkerOptions(resultList.getMarkerOptions());
                        makerOptionsMap.put(m.getId(),r);
                    }
                }
                currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("現在位置").flat(true));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                startDirectionRequest();
                Log.d(TAG,"viewMarker");
            }
            Log.d(TAG,"callback ended");
        }
        @Override
        public void onFailure(Call<PlaceResponce> call, Throwable t) {
            t.printStackTrace();
        }
    };

    /**
     * ルートアイテムのドラッグ処理のためのリスナー
     */
    class DragListener extends RootListView.SimpleDragListener {
        @Override
        public int onStartDrag(int position) {
            draggingPosition = position;
            rootListView.invalidateViews();
            return position;
        }

        @Override
        public int onDuringDrag(int positionFrom, int positionTo) {
            if (positionFrom < 0 || positionTo < 0
                    || positionFrom == positionTo) {
                return positionFrom;
            }
            int i;
            if (positionFrom < positionTo) {
                final int min = positionFrom;
                final int max = positionTo;
                final String data = rootList.get(min).getTitle();
                i = min;
                while (i < max) {
                    rootList.get(i).setTitle(rootList.get(++i).getTitle());
                }
                rootList.get(max).setTitle(data);
            } else if (positionFrom > positionTo) {
                final int min = positionTo;
                final int max = positionFrom;
                final String data = rootList.get(max).getTitle();
                i = max;
                while (i > min) {
                    rootList.get(i).setTitle(rootList.get(--i).getTitle());
                }
                rootList.get(min).setTitle(data);
            }
            draggingPosition = positionTo;
            rootListView.invalidateViews();
            return positionTo;
        }

        @Override
        public boolean onStopDrag(int positionFrom, int positionTo) {
            draggingPosition = -1;
            rootListView.invalidateViews();
            startDirectionRequest();
            return super.onStopDrag(positionFrom, positionTo);
        }
    }

    /**
     * ブックマーク表示のadapter
     */
    class BookMarkAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;
        private MainActivity mainActivity;
        String item;

        public BookMarkAdapter(Context context, int resource) {
            super(context, resource);
        }

        public BookMarkAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public BookMarkAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }


        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = inflater.inflate(R.layout.bookmark_itemlayout,null);
            }
            item = this.getItem(position);

            ImageButton imageButton = (ImageButton)convertView.findViewById(R.id.bookmark_delete);
            imageButton.setTag(item);

            TextView textView = (TextView) convertView.findViewById(R.id.bookmark_text);
            if(textView != null){
                textView.setText(item);
            }
            return convertView;

        }
    }

    /**
     * ブックマーク追加のadapter
     */
    class AddListAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;
        private MainActivity mainActivity;
        String item;

        public AddListAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = inflater.inflate(R.layout.addbookmark_itemlayout,null);
            }
            item = this.getItem(position);

            TextView textView = (TextView) convertView.findViewById(R.id.bookmark_text);
            if(textView != null){
                textView.setText(item);
            }
            return convertView;

        }
    }

    /**
     * ブックマークの削除処理
     * @param v
     */
    public void removeItem(View v){
        if(itemListHashMap.get(String.valueOf(v.getTag())).equals(selectedList)){
            selectedList = null;
            bookmarkListView.setSelectView(null);
        }
        bookmarkAdapter.remove(String.valueOf(v.getTag()));
        itemListHashMap.remove(String.valueOf(v.getTag()));
        bookmarkAdapter.notifyDataSetChanged();
    }

}




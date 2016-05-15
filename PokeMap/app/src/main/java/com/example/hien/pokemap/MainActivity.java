package com.example.hien.pokemap;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.Loader;
import android.support.v4.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener {

    private FloatingActionButton btnMyLocation, btnNearlyLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    public Location myLocation;
    private GoogleMap googleMap;
    private final String CONNECTION_TAG = "GService";
    //    private MyMapFragment mapFragment;
    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private ImageView geo_autocomplete_clear;
    private static final String GOOGLE_API_KEY = "AIzaSSDFSDF8Kv2eP0PM8adf5dSDFysdfas323SD3HA";
    double latitude = 0;
    double longitude = 0;
    private int PROXIMITY_RADIUS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setSupportActionBar(toolbar);

        btnMyLocation = (FloatingActionButton) findViewById(R.id.fab);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimateToMyLocation();
            }
        });
        btnNearlyLocation = (FloatingActionButton) findViewById(R.id.nearly);
        btnNearlyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchNearly();
            }
        });
        //Init nav
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Init GoogleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        geo_autocomplete_clear = (ImageView) findViewById(R.id.geo_autocomplete_clear);

        geo_autocomplete = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete);
        geo_autocomplete.setThreshold(THRESHOLD);
        geo_autocomplete.setAdapter(new GeoAutoCompleteAdapter(this)); // 'this' is Activity

        geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                geo_autocomplete.setText(result.getAddress());
            }
        });

        geo_autocomplete.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    geo_autocomplete_clear.setVisibility(View.VISIBLE);
                } else {
                    geo_autocomplete_clear.setVisibility(View.GONE);
                }
            }
        });

        geo_autocomplete.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String g = geo_autocomplete.getText().toString();

                    Geocoder geocoder = new Geocoder(getBaseContext());
                    List<Address> addresses = null;

                    try {
                        // Getting a maximum of 3 Address that matches the input
                        // text
                        addresses = geocoder.getFromLocationName(g, 3);
                        if (addresses != null && !addresses.equals(""))
                            search(addresses);

                    } catch (Exception e) {

                    }
                    return true;
                }
                return false;
            }
        });

        geo_autocomplete_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                geo_autocomplete.setText("");
            }
        });
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
//    private MyMapFragment ShowMapFragment()
//    {
//        MyMapFragment fragment = new MyMapFragment();
//        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.frame, fragment, "MapFragment");
//        fragmentTransaction.commit();
//        return fragment;
//    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.action_settings:
                return true;
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_roadmap)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if (id == R.id.nav_satellite)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else if (id == R.id.nav_terrain)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.nav_hybrid)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        else if (id == R.id.nav_share)
        {

        }
        else if (id == R.id.nav_send)
        {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        googleApiClient.connect();  //Connect to Server
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        googleApiClient.disconnect();   //Disconnect from Server
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        //Assign user's location to the last location.
        myLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        //Show map
        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        googleMap = mapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        Log.w("", mapFragment.toString());
    }



    @Override
    public void onConnectionSuspended(int i)
    {
        Log.w(CONNECTION_TAG, "Connection suspended");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.w(CONNECTION_TAG, "Location changed");
        myLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.w(CONNECTION_TAG, "Connection failed");
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.w(CONNECTION_TAG, "Map is ready");
        this.googleMap = googleMap;
        if(myLocation != null)
            AnimateToMyLocation();
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
    }

    public void AnimateToMyLocation()
    {
        String provider = Settings.Secure.getString(getContentResolver(),     Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider != null ){
            if(! provider.contains("gps")){
                // Notify users and show settings if they want to enable GPS
                new AlertDialog.Builder(this)
                        .setMessage("Use location?")
                        .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, 5);
                            }
                        })
                        .setNegativeButton("Don't do it", new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .show();
            }
        }
        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Your current location");

        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }

    protected void search(List<Address> addresses) {

        Address address = (Address) addresses.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(0) : "", address.getCountryName());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(addressText);

        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            geo_autocomplete.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void searchNearly(){
        String type = geo_autocomplete.getText().toString();
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        Object[] toPass = new Object[2];
        toPass[0] = googleMap;
        toPass[1] = googlePlacesUrl.toString();
        googlePlacesReadTask.execute(toPass);
    }
}

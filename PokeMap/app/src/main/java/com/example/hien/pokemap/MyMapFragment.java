//package com.example.hien.pokemap;
//
//
//import android.app.Activity;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.MapsInitializer;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//
///**
// * Created by Hien on 10/04/2016.
// */
//public class MyMapFragment extends Fragment implements OnMapReadyCallback
//{
//    public String name = "MapFragment";
//    MapView mapView;
//    private static GoogleMap map;
//    public static Location myLocation;
//
//    public MyMapFragment()
//    {
//        super();
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_map, container, false);
//
//        // Gets the MapView from the XML layout and creates it
//        mapView = (MapView) v.findViewById(R.id.map);
//        mapView.onCreate(savedInstanceState);
//
//        // Gets to GoogleMap from the MapView and does initialization stuff
//        map = mapView.getMap();
//        map.getUiSettings().setMyLocationButtonEnabled(false);
//        map.setMyLocationEnabled(true);
//
//        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
//        MapsInitializer.initialize(this.getActivity());
//        return v;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState)
//    {
//        super.onActivityCreated(savedInstanceState);
//        MainActivity mainActivity = (MainActivity) this.getActivity();
//        myLocation = mainActivity.myLocation;
//    }
//
//    @Override
//    public void onResume() {
//        mapView.onResume();
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }
//
//    public static void AnimateToMyLocation()
//    {
//        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng)
//                .title("Your current location");
//        map.addMarker(markerOptions);
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap)
//    {
//        AnimateToMyLocation();
//    }
//}
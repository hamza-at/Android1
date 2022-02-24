package com.example.geosafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.geosafe.direction.FetchURL;
import com.example.geosafe.direction.TaskLoadedCallback;
import com.example.geosafe.model.Localisation;
import com.example.geosafe.utils.Tools;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener, TaskLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient ;
    DatabaseReference userTracedLocation, location;
    Marker marker;
    private Polyline currentPolyline;
    private MarkerOptions place1, place2;
    Button getDirection;
    FusedLocationProviderClient mLocationClient;
    FloatingActionButton loca;
    private LocationManager locationManager;
    private String provider;


    @SuppressLint("VisibleForTests")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        loca = findViewById(R.id.loca);
        getDirection = findViewById(R.id.btnGetDirection);

        checkLocationPermission();
        getDirection.setOnClickListener(view -> new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition()), "driving"));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        loca.setOnClickListener(v -> getLocation());
        mLocationClient = new FusedLocationProviderClient(this);


        if (Tools.UserTraced != null) recorderRealtime();
    }

    private void getLocation() {

        checkLocationPermission();

        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                Log.wtf("location", "" + location);
                if (location != null)
                    gotoLocation(location.getLatitude(), location.getLongitude());
            }
        });

    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng latLng=new LatLng(latitude,longitude);
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng,18);
        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Activate zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);

        checkLocationPermission();
        //click on the button to display logged user position
        mMap.setMyLocationEnabled(true);

        //setting a map style
       googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.maps_style));
    }
    private void recorderRealtime() {
        if(userTracedLocation==null){
            userTracedLocation = FirebaseDatabase.getInstance()
                    .getReference(Tools.LOCATION)
                    .child(Tools.UserTraced.getUid());
            userTracedLocation.addValueEventListener(this);
        }

    }
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.getValue() !=null)
        {
            Log.wtf("snapshot",""+snapshot);
         Localisation location =snapshot.getValue(Localisation.class);
            // Add a marker
            assert location != null;
            LatLng userMarker = new LatLng(location.getLatitude(),location.getLongitude());
            if (null != marker) {
                marker.remove();
            }
            place2=new MarkerOptions().position(userMarker).title(Tools.UserTraced.getEmail());
            marker=mMap.addMarker(new MarkerOptions().position(userMarker)
                    .title(Tools.UserTraced.getEmail())
                    .snippet(Tools.getDateFormatted(Tools.convertTimeStampsToDate(location.getTime()))));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16f));


        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }


    @Override
  protected void onResume() {
        super.onResume();
        if (userTracedLocation != null) {
            userTracedLocation = FirebaseDatabase.getInstance()
                    .getReference(Tools.LOCATION)
                    .child(Tools.UserTraced.getUid());
            userTracedLocation.addValueEventListener(this);
        }
    }

    @Override
    protected void onStop() {
        if (userTracedLocation != null) {
            userTracedLocation.removeEventListener(this);
        }
        super.onStop();

    }
    //direction API
    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + "driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyAGopd_tOyUJTzSRjF2qFBXnHY2m4tD5DE";
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user After the user
                // sees the explanation, We try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // Request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 400, 1, (LocationListener) this);
                    }

                } else {

                    // permission denied

                }
                return;
            }

        }
    }
}
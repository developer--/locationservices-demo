package geolab.ge.locationsdemo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;
    TextView textView;
    TextView addressTextView;
    private GoogleMap mMap;
    private GeofencingClient mGeofencingClient;
    private GeofencingRequest request;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textView = findViewById(R.id.coordinates_txt_id);
        addressTextView = findViewById(R.id.address_txt_id);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationUpdates();
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            new GeocoderAsyncTask().execute(location);
                            textView.setText(location.getLatitude() + " : " + location.getLongitude());

                            LatLng currLocation = new LatLng(location.getLatitude(),location.getLongitude());
                            LatLng delisi = new LatLng(41.7240604,44.743502);
                            addMarker(currLocation);
                            addMarker(delisi);

                            Geofence geofence = new Geofence.Builder()
                                    .setRequestId("geolab") // Geofence ID
                                    .setCircularRegion( location.getLatitude(), location.getLongitude(), 100) // defining fence region
                                    .setExpirationDuration( 10000 ) // expiring date
                                    .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT )
                                    .build();

                            request = new GeofencingRequest.Builder()
                                    // Notification to trigger when the Geofence is created
                                    .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                                    .addGeofence( geofence ) // add a Geofence
                                    .build();

                            mGeofencingClient.addGeofences(request,getGeofencePendingIntent())
                                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "added success", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "failure", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }

    private PendingIntent mGeofencePendingIntent;
    private PendingIntent getGeofencePendingIntent(){
        if (mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(100000);
        locationRequest.setFastestInterval(50000);
        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLocation = locationResult.getLastLocation();
                textView.setText(lastLocation.getLatitude() + " : " + lastLocation.getLongitude());
                Toast.makeText(MainActivity.this, "updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        }, null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void addMarker(LatLng location){
        CircleOptions circleOptions = new CircleOptions()
                .center( new LatLng(location.latitude, location.longitude) )
                .radius(100)
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
        mMap.addMarker(new MarkerOptions().position(location).title("Marker in Sydney"));
        mMap.addCircle(circleOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    class GeocoderAsyncTask extends AsyncTask<Location, Void, String>{

        @Override
        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(MainActivity.this);
            double lat = locations[0].getLatitude();
            double lon = locations[0].getLongitude();
            try {
                List<Address> cities = geocoder.getFromLocation(lat,lon,1);
                if (!cities.isEmpty()) {
                    Address address = cities.get(0);
                    return address.getThoroughfare();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String street) {
            super.onPostExecute(street);
            addressTextView.setText(street);
        }
    }

}

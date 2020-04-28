package com.easy.sumit.pitcon;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private double latitude=0,longitude=0;
    private LatLng motoCon;
    private LocationUpdater locationUpdater;
    private Marker marker=null;
    private MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationUpdater=new LocationUpdater();
        markerOptions=new MarkerOptions();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        locationUpdater.stopLocationUpdates();
        super.onStop();
    }
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Add a marker in Sydney and move the camera
        motoCon = new LatLng(latitude,longitude);
        markerOptions.position(motoCon).title("MotoCon");
        marker=mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(motoCon));
        locationUpdater.startLocationUpdates();
    }
    private class LocationUpdater{
        RequestQueue requestQueue;
        StringRequest stringRequest;
        Handler handler;
        Runnable runnableCode;
        public LocationUpdater(){
            stringRequest=new StringRequest(Request.Method.POST,
                    "http://lifelinebloodbank.esy.es/gps/getlocation.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response);
                                double lat=Double.parseDouble(jsonObject.getString("lat"));
                                double lon=Double.parseDouble(jsonObject.getString("lon"));
                                Log.i("Location","Longitude:"+longitude+"\nLatitude:"+latitude);
                                if(lat!=latitude||lon!=longitude){
                                    latitude=lat;
                                    longitude=lon;
                                    updateMap();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
            requestQueue=Volley.newRequestQueue(MapsActivity.this);
        }
        private void updateLocation(){
            requestQueue.add(stringRequest);
        }
        public void startLocationUpdates(){
            handler = new Handler();
            runnableCode = new Runnable() {
                @Override
                public void run() {
                    updateLocation();
                    handler.postDelayed(runnableCode, 2000);
                }
            };
            handler.post(runnableCode);

        }
        private void updateMap(){
            motoCon = new LatLng(latitude,longitude);
            marker.setPosition(motoCon);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(motoCon));
        }

        public void stopLocationUpdates(){
            if(handler!=null){
                handler.removeCallbacks(runnableCode);
            }
        }
    }
}

package com.example.demogooglemap;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "MainActivity2";

    private GeoApiContext mGeoApiContext = null;
    private Marker mStartPosMarker, mDestinationPosMarker;
    private GoogleMap mGoogleMap;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();

    // views
    private MapView mMapView;
    private TextView tvLatPos1, tvLngPos1, tvLatPos2, tvLngPos2, tvDistance, tvTime;
    private Button btnBus, btnCar;

    // 10.875334512286456, 106.80071266724018
    // 10.718460164145629, 106.6283236249088

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mMapView = findViewById(R.id.mapView);
        tvLatPos1 = findViewById(R.id.tvLatPos1);
//        tvLngPos1 = findViewById(R.id.tvLngPos1);
        tvLatPos2 = findViewById(R.id.tvLatPos2);
//        tvLngPos2 = findViewById(R.id.tvLngPos2);
        btnBus = findViewById(R.id.btnBus);
        btnCar = findViewById(R.id.btnCar);
        tvDistance = findViewById(R.id.tvDistance);
        tvTime = findViewById(R.id.tvTime);

        initGoogleMap(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnPolylineClickListener(this);

        btnCar.setOnClickListener(v -> {
            googleMap.clear();
            clearResultViews();

            LatLng pos1 = onQueryTextSubmit(tvLatPos1.getText().toString());
            LatLng pos2 = onQueryTextSubmit(tvLatPos2.getText().toString());
            if (pos1 == null || pos2 == null) return;

            addMarkers(googleMap, pos1, pos2);
//            drawPolyline(googleMap, pos1, pos2);
            calculateDirections(mDestinationPosMarker, mStartPosMarker, TravelMode.DRIVING);
        });

        btnBus.setOnClickListener(v -> {
            googleMap.clear();
            clearResultViews();

            LatLng pos1 = getLatLngFromTextView(tvLatPos1, tvLngPos1);
            LatLng pos2 = getLatLngFromTextView(tvLatPos2, tvLngPos2);
            if (pos1 == null || pos2 == null) return;

            addMarkers(googleMap, pos1, pos2);
//            drawPolyline(googleMap, pos1, pos2);
            calculateDirections(mDestinationPosMarker, mStartPosMarker, TravelMode.TRANSIT);
        });
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    private void clearResultViews() {
        tvDistance.setText("");
        tvTime.setText("");
    }


    private void addMarkers(GoogleMap googleMap, LatLng pos1, LatLng pos2) {

        mStartPosMarker = googleMap.addMarker(new MarkerOptions().position(pos1).title("Start Position"));
        mDestinationPosMarker = googleMap.addMarker(new MarkerOptions().position(pos2).title("Destication Position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos1, 10f));
    }

    private LatLng getLatLngFromTextView(TextView tvLat, TextView tvLng) {
        if (tvLat.getText().toString().isEmpty() || tvLng.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please input latitude and longitude", Toast.LENGTH_SHORT).show();
            return null;
        }
        double lat = Double.parseDouble(tvLat.getText().toString());
        double lng = Double.parseDouble(tvLng.getText().toString());
        return new LatLng(lat, lng);
    }

    private void calculateDirections(Marker destinationPosMarker, Marker startPosMarker, TravelMode travelMode) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                destinationPosMarker.getPosition().latitude,
                destinationPosMarker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.mode(travelMode);
        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        startPosMarker.getPosition().latitude,
                        startPosMarker.getPosition().longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage());

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if (mPolyLinesData.size() > 0) {
                    for (PolylineData polylineData : mPolyLinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }
                double duration = 999999999;
                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                    polyline.setWidth(10);
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    // highlight the fastest route and adjust camera
                    double tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration < duration) {
                        duration = tempDuration;
                        onPolylineClick(polyline);
                    }
                }
            }
        });
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        int index = 0;
        for (PolylineData polylineData : mPolyLinesData) {
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                tvTime.setText(polylineData.getLeg().duration + "");
                tvDistance.setText(polylineData.getLeg().distance + "");
            } else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }


    public LatLng onQueryTextSubmit(String location) {
        List<Address> addressList = null;
        LatLng latLng = null;
        if (location != null) {
            Geocoder geocoder = new Geocoder(MainActivity2.this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            latLng = new LatLng(address.getLatitude(), address.getLongitude());
        }
        return latLng;
    }
}
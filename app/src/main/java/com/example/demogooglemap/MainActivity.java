package com.example.demogooglemap;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final PatternItem DOT = new Dot();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // VD1: Add a marker in Nha Van Hoa Sinh Vien and move the camera
//        // Add a marker in Nha Van Hoa Sinh Vien and move the camera
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(nvhsv).zoom(18f).build();
//
//        googleMap.addMarker(new MarkerOptions().position(nvhsv).title("Nha Van Hoa Sinh Vien"));
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //------------------------------------------------------------------------------------------
        // VD2: Draw a line between Nha Van Hoa Sinh Vien and my house. And add click event for the line

        // Nha Van Hoa Sinh Vien position
        LatLng nvhsv = new LatLng(10.875334512286456, 106.80071266724018);
        // My house position
        LatLng myHouse = new LatLng(10.718460164145629, 106.6283236249088);

        // Add markers for Nha Van Hoa Sinh Vien and my house. Then move the camera to the position
        // where we can see both markers
        googleMap.addMarker(new MarkerOptions().position(nvhsv).title("Nha Van Hoa Sinh Vien"));
        googleMap.addMarker(new MarkerOptions().position(myHouse).title("My house"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nvhsv, 10f));

        // Draw a line between Nha Van Hoa Sinh Vien and my house. Show the distance between them
        // and estimate the time to go from Nha Van Hoa Sinh Vien to my house and vice versa, by
        // walking, driving, bicycling, and transit
        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                .add(nvhsv, myHouse)
                .clickable(true)
                .width(10)
                .color(Color.RED));
        // polyline.setWidth(10);
        // polyline.setColor(Color.RED);

        // Set listeners for click events
        googleMap.setOnPolylineClickListener(this);

        //------------------------------------------------------------------------------------------
        // VD3: Calculate the distance and estimate time to go from Nha Van Hoa Sinh Vien to my house
        // by walking, driving, bicycling, and transit
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            List<PatternItem> pattern = new ArrayList<>();
            pattern.add(DOT);
            polyline.setPattern(pattern);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        // Calculate the distance between Nha Van Hoa Sinh Vien and my house
        BigDecimal distance = haversineDistance(polyline.getPoints().get(0), polyline.getPoints().get(1));
        Toast.makeText(this, "Distance: " + distance + " km", Toast.LENGTH_SHORT).show();
    }

    public static BigDecimal haversineDistance(LatLng mk1, LatLng mk2) {
        double R = 6371.0; // Radius of the Earth in kilometers
        double rlat1 = Math.toRadians(mk1.latitude); // Convert degrees to radians
        double rlat2 = Math.toRadians(mk2.latitude); // Convert degrees to radians
        double difflat = rlat2 - rlat1; // Radian difference (latitudes)
        double difflon = Math.toRadians(mk2.longitude - mk1.longitude); // Radian difference (longitudes)

        double a = Math.sin(difflat / 2) * Math.sin(difflat / 2) +
                Math.cos(rlat1) * Math.cos(rlat2) *
                        Math.sin(difflon / 2) * Math.sin(difflon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = R * c; // Distance in kilometers
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_EVEN);
    }

}
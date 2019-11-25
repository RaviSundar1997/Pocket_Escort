package com.example.ravisundar.ullala_driver;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Driver_display extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    LocationListener locationListener;
    LocationManager locationManager;
    EditText yourplace, destinationplace;
    Button search, search1, place_checck, routes, trackMe, panic_button;
    int PLACE_PICKER_REQUEST = 1;
    int a = 0, count = 0;
    Double latitude1 = 0.0, longitude1 = 0.0, latitude2 = 0.0, longitude2 = 0.0;
    String number, toastMsg1, toastMsg2;
    float[] results = new float[10];
    String url;
    private DatabaseReference databaseReference, databaseReference2;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private List<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_display);
        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("customer");
        databaseReference2 = FirebaseDatabase.getInstance().getReference("driver");
        yourplace = findViewById(R.id.place);
        destinationplace = findViewById(R.id.destinationplace);
        search1 = findViewById(R.id.search1);
        search = findViewById(R.id.search);
        place_checck = findViewById(R.id.place_check);
        routes = findViewById(R.id.route);
        panic_button = findViewById(R.id.panic_button);
        trackMe = findViewById(R.id.trackMe);
        Bundle extras = getIntent().getExtras();
        number = extras.getString("number");

        panic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialContactPhone("123123123");
            }
        });

        routes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }

        });


        trackMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = "";
                String police = "police";
                String url = getURL(latitude1, latitude2, police);
                Object[] dataTrasfer = new Object[2];
                dataTrasfer[0] = mMap;
                dataTrasfer[1] = url;
                GetNearbyPlace getNearbyPlace = new GetNearbyPlace();
                getNearbyPlace.execute(dataTrasfer);
                /*try {
                    databaseReference.child(number).child("latitude1").setValue(latitude1);
                    databaseReference.child(number).child("longitude1").setValue(longitude1);
                    databaseReference.child(number).child("source").setValue(toastMsg1);
                    databaseReference.child(number).child("latitude2").setValue(latitude2);
                    databaseReference.child(number).child("longitude2").setValue(longitude2);
                    final float val11= (float) (((results[0])*0.001609));
                    databaseReference.child(number).child("distance").setValue(val11);
                    databaseReference.child(number).child("destination").setValue(toastMsg2).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            String s= String.valueOf(val11);
                            Intent i = new Intent(getApplicationContext(), Driver_display.class);
                            i.putExtra("number",number);
                            i.putExtra("distance",s);
                            startActivity(i);
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }


*/

            }
        });


        //source
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a = 0;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Driver_display.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    String x = e.toString();
                    Log.e("@@@", x);
                }
            }
        });

        //destination

        search1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Driver_display.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    String x = e.toString();
                    Log.e("@@@", x);
                }
            }
        });

        place_checck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 0;
                String hospital = "hospital";
                String url = getURL(latitude1, latitude2, hospital);
                Object[] dataTrasfer = new Object[2];
                dataTrasfer[0] = mMap;
                dataTrasfer[1] = url;
                Driver_display.MyAsycTaskToGetPlace myAsycTaskToGetPlace1 = new Driver_display.MyAsycTaskToGetPlace();
                myAsycTaskToGetPlace1.execute(dataTrasfer);

                String police = "police";
                String url1 = getURL(latitude1, latitude2, police);
                Object[] dataTrasfer1 = new Object[2];
                dataTrasfer1[0] = mMap;
                dataTrasfer1[1] = url1;
                Driver_display.myAsycTaskToGetPlace myAsycTaskToGetPlace2 = new Driver_display.myAsycTaskToGetPlace();
                myAsycTaskToGetPlace2.execute(dataTrasfer1);

            }
        });

    }

    private String getDirectionUrl() {

        StringBuilder urlhere = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude1 + "," + longitude1 + "&" + "destination=" + latitude2 + "," + longitude2 + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return urlhere.toString();
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void getRouteToMarker() {
        LatLng start = new LatLng(latitude1, longitude1);
        LatLng end = new LatLng(latitude2, longitude2);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build();
        routing.execute();
    }

    private String getURL(Double latitude1, Double latitude2, String nearbyplace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude1 + "," + longitude1 + "&radius=1500&type=" + nearbyplace + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return googlePlaceUrl.toString();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place;
                Bundle extras = getIntent().getExtras();
                place = PlacePicker.getPlace(Driver_display.this, data);

                if (a == 0) {
                    toastMsg1 = String.format("%s", place.getName());
                    yourplace.setText(toastMsg1);
                    latitude1 = place.getLatLng().latitude;
                    longitude1 = place.getLatLng().longitude;
                    number = place.getPhoneNumber().toString();

                    LatLng source = new LatLng(latitude1, longitude1);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 16));
                    mMap.addMarker(new MarkerOptions().position(source).title("Your place"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(source));
                } else {
                    toastMsg2 = String.format("%s", place.getName());
                    destinationplace.setText(toastMsg2);
                    latitude2 = place.getLatLng().latitude;
                    longitude2 = place.getLatLng().longitude;
                    // Toast.makeText(this, latitude1 + " " + longitude1, Toast.LENGTH_LONG).show();
                    LatLng destination = new LatLng(latitude2, longitude2);
                    //  databaseReference.child("driver").child(id1).setValue(latitude1,longitude1);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 16));
                    mMap.addMarker(new MarkerOptions().position(destination).title("Your destination"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                }


            }
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }

    private void dialContactPhone(final String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:1901"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        }
        startActivity(callIntent);
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    public class MyAsycTaskToGetPlace extends AsyncTask<Object, String, String> {
        GoogleMap mMap;
        String url;
        private

        String googlePlaceData;

        @Override
        protected String doInBackground(Object... objects) {

            mMap = (GoogleMap) objects[0];
            url = (String) objects[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            try {
                googlePlaceData = downloadUrl.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return googlePlaceData;
        }

        @Override
        protected void onPostExecute(String s) {
            List<HashMap<String, String>> nearbyplaceList = null;
            DataParser parser = new DataParser();
            nearbyplaceList = parser.parse(s);
            showNearbyPlaces(nearbyplaceList);


        }

        private List<HashMap<String, String>> showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
            if (nearbyPlaceList.size() >= 1) {
                for (int i = 0; i < nearbyPlaceList.size(); i++) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                    String placeName = googlePlace.get("place_name");
                    String vicinity = googlePlace.get("vicinity");
                    double lat = Double.parseDouble(googlePlace.get("lat"));
                    double lng = Double.parseDouble(googlePlace.get("lng"));

                    LatLng latlng = new LatLng(lat, lng);
                    //markerOptions.position(latlng);
                    //markerOptions.title(placeName+"  "+vicinity);
                    mMap.addMarker(new MarkerOptions().position(latlng).title(placeName));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                    count++;


                }

            }

            return nearbyPlaceList;
        }
    }


    public class myAsycTaskToGetPlace extends AsyncTask<Object, String, String> {
        GoogleMap mMap;
        String url;
        private

        String googlePlaceData;

        @Override
        protected String doInBackground(Object... objects) {

            mMap = (GoogleMap) objects[0];
            url = (String) objects[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            try {
                googlePlaceData = downloadUrl.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return googlePlaceData;
        }

        @Override
        protected void onPostExecute(String s) {
            List<HashMap<String, String>> nearbyplaceList = null;
            DataParser parser = new DataParser();
            nearbyplaceList = parser.parse(s);
            showNearbyPlaces(nearbyplaceList);


        }

        private List<HashMap<String, String>> showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
            if (nearbyPlaceList.size() >= 1) {
                for (int i = 0; i < nearbyPlaceList.size(); i++) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                    String placeName = googlePlace.get("place_name");
                    String vicinity = googlePlace.get("vicinity");
                    double lat = Double.parseDouble(googlePlace.get("lat"));
                    double lng = Double.parseDouble(googlePlace.get("lng"));

                    LatLng latlng = new LatLng(lat, lng);
                    //markerOptions.position(latlng);
                    //markerOptions.title(placeName+"  "+vicinity);
                    mMap.addMarker(new MarkerOptions().position(latlng).title(placeName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                    count++;


                }

            }

            if (count == 2) {
                Toast.makeText(getApplicationContext(), "Secure", Toast.LENGTH_LONG).show();
            } else if (count == 1) {
                Toast.makeText(getApplicationContext(), "Moderate safety", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "NotSecure", Toast.LENGTH_LONG).show();
            }

            return nearbyPlaceList;
        }
    }
}

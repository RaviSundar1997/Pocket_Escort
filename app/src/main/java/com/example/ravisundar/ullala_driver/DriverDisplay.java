package com.example.ravisundar.ullala_driver;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class DriverDisplay extends FragmentActivity implements OnMapReadyCallback {
    TextView sourceplace, destinationplace, places;
    Button search, search1, route;
    GoogleMap mMap;
    int PLACE_PICKER_REQUEST = 1, a = 0, count = 0;
    LatLng source, destination;
    String[] directionsList;
    String[][] directionLists;
    List<LatLng> temp;
    Double score = 0.0;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_display2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sourceplace = findViewById(R.id.place);
        destinationplace = findViewById(R.id.destinationplace);
        search = findViewById(R.id.search);
        search1 = findViewById(R.id.search1);
        route = findViewById(R.id.route);
        places = findViewById(R.id.places);


        //source
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a = 0;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(DriverDisplay.this), PLACE_PICKER_REQUEST);
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
                startLocationUpdates();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(DriverDisplay.this), PLACE_PICKER_REQUEST);
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

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String start = sourceplace.getText().toString();
                String end = destinationplace.getText().toString();
                Geocoder myLocation = new Geocoder(DriverDisplay.this, Locale.getDefault());
                List<Address> myList = null, myList2 = null;
                try {
                    myList = myLocation.getFromLocation(source.latitude, source.longitude, 1);
                    myList2 = myLocation.getFromLocation(destination.latitude, destination.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String loca = myList.get(0).getLocality();
                String loc2 = myList2.get(0).getLocality();

                if (start.matches("") || end.matches("")) {
                    Toast.makeText(getApplicationContext(), "enter the place properly", Toast.LENGTH_SHORT).show();
                } else {
                    if (loca.matches("Chicago") && loc2.matches("Chicago")) {
                        Toast.makeText(getApplicationContext(), loca + " " + loc2, Toast.LENGTH_SHORT).show();
                        String url = getDirectionUrl();
                        Object[] dataTrasferDirection = new Object[2];
                        dataTrasferDirection[0] = mMap;
                        dataTrasferDirection[1] = url;
                        DriverDisplay.GetDirectionsData getDirectionsData = new DriverDisplay.GetDirectionsData();
                        getDirectionsData.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Only For Chicago", Toast.LENGTH_SHORT).show();
                    }

                }


            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place;
                //Bundle extras = getIntent().getExtras();
                place = PlacePicker.getPlace(DriverDisplay.this, data);
                places.setText("Route score here");

                if (a == 0) {
                    sourceplace.setText(String.format("%s", place.getName()));
                    source = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 16));
                    mMap.addMarker(new MarkerOptions().position(source).title("Your place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(source));
                } else {
                    destinationplace.setText(String.format("%s", place.getName()));
                    destination = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 16));
                    mMap.addMarker(new MarkerOptions().position(destination).title("Your destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                }


            }
        }


    }

    private String getDirectionUrl() {
        StringBuilder urlhere = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=" + source.latitude + "," + source.longitude + "&" + "destination=" + destination.latitude + "," + destination.longitude + "&" + "sensor=false&alternatives=true&units=metric&mode=driving" + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        //  StringBuilder urlhere=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return urlhere.toString();
    }

    private String getURL(String nearbyplace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + source.latitude + "," + source.longitude + "&radius=1500&type=" + nearbyplace + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return googlePlaceUrl.toString();

    }

    public void displayDirection(String[] directionList) {
        for (int i = 0; i < directionList.length; i++) {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.BLUE);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));
            mMap.addPolyline(options);
            temp = PolyUtil.decode(directionList[i]);

        }
        String url = getDirectionUrl();
        Object[] dataTrasferDirection = new Object[2];
        dataTrasferDirection[0] = mMap;
        dataTrasferDirection[1] = url;
        DriverDisplay.myApi getDirectionsData = new DriverDisplay.myApi();
        getDirectionsData.execute();


    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());

    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                location.getLatitude() + "," +
                location.getLongitude();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
    }

    public class myApi extends AsyncTask<Object, String, String> {
        Double lat = source.latitude;
        Double lng = source.longitude;
        StringBuilder url = new StringBuilder("https://chicago-safet-score-api.herokuapp.com/?lat=" + lat + "&lng=" + lng);
        String myUrl = url.toString();

        StringBuffer responce = new StringBuffer();
        String value = " ";


        @Override
        protected String doInBackground(Object... objects) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL(myUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;

                while ((inputLine = br.readLine()) != null) {
                    responce.append(inputLine);
                    value = responce.toString();


                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.d("adfgh", responce.toString());
            return responce.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            String[] arr = value.split(":");
            String values = arr[1];
            Double val = Double.valueOf(values.substring(1, values.length() - 2));
            score += val;
            count++;

            if (val > 0.2 && val < 0.4) {
                for (int i = 0; i < directionsList.length; i++) {
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.RED);
                    options.width(10);
                    options.addAll(PolyUtil.decode(directionsList[i]));
                    mMap.addPolyline(options);
                }

            } else if (val >= 0.4 && val <= 0.8) {
                for (int i = 0; i < directionsList.length; i++) {
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.GREEN);
                    options.width(10);
                    options.addAll(PolyUtil.decode(directionsList[i]));
                    mMap.addPolyline(options);
                }

            } else {
                for (int i = 0; i < directionsList.length; i++) {
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.BLACK);
                    options.width(10);
                    options.addAll(PolyUtil.decode(directionsList[i]));
                    mMap.addPolyline(options);
                }

            }
            Double finalscore = (score / count) * 100;
            places.setText("Route score is :" + finalscore);
            Toast.makeText(getApplicationContext(), finalscore + " " + directionsList.length, Toast.LENGTH_LONG).show();
            String hospital = "hospital";
            String url1 = getURL(hospital);
            Object[] dataTrasfer = new Object[2];
            dataTrasfer[0] = mMap;
            dataTrasfer[1] = url1;
            DriverDisplay.myAsycTaskToGetPlaceForDirection myAsycTaskToGetPlace1 = new DriverDisplay.myAsycTaskToGetPlaceForDirection();
            myAsycTaskToGetPlace1.execute(dataTrasfer);

            String police = "police";
            String url2 = getURL(police);
            Object[] dataTrasfer1 = new Object[2];
            dataTrasfer1[0] = mMap;
            dataTrasfer1[1] = url2;
            DriverDisplay.myAsycTaskToGetPlaceForDirectionPolice myAsycTaskToGetPlace2 = new DriverDisplay.myAsycTaskToGetPlaceForDirectionPolice();
            myAsycTaskToGetPlace2.execute(dataTrasfer1);

        }
    }

    public class GetDirectionsData extends AsyncTask<Object, String, String> {
        String url = getDirectionUrl();
        Double score;
        String googleDirectionData, valu;
        int size = 0;

        @Override
        protected String doInBackground(Object... objects) {
            DownloadUrl downloadUrl = new DownloadUrl();
            try {
                googleDirectionData = downloadUrl.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return googleDirectionData;
        }

        @Override
        protected void onPostExecute(String s) {
            //  String []directionsList;
            Routesclass parser = new Routesclass();
            directionsList = parser.parseDirection(s, 0);


            displayDirection(directionsList);
            size = parser.routeSize();
            directionLists = new String[size][0];

            for (int i = 1; i < size; i++) {
                directionsList = parser.parseDirection(s, i);
                directionLists[i] = directionsList;
            }

            displayDirection(directionsList);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 20));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(source));

        }


    }

    public class myAsycTaskToGetPlaceForDirection extends AsyncTask<Object, String, String> {
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


                }


            }


            return nearbyPlaceList;
        }
    }

    public class myAsycTaskToGetPlaceForDirectionPolice extends AsyncTask<Object, String, String> {
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
                    mMap.addMarker(new MarkerOptions().position(latlng).title(placeName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


                }
                //LatLng source = new LatLng(latitude1, longitude1);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 20));


            }


            return nearbyPlaceList;
        }
    }


}

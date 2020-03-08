package com.example.ravisundar.ullala_driver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class Driver_display1 extends FragmentActivity implements OnMapReadyCallback {

    EditText yourplace, destinationplace;
    Button search, search1, place_checck, routes, trackMe, panic_button;
    int PLACE_PICKER_REQUEST = 1;
    int a = 0, count;

    Double latitude1 = 0.0, longitude1 = 0.0, latitude2 = 0.0, longitude2 = 0.0;
    String number, toastMsg1, toastMsg2, waypoint = "vadapalani";
    float[] results = new float[10];
    private GoogleMap mMap;
    List<LatLng> temp;
    private DatabaseReference databaseReference, databaseReference2;
    LocationTrack locationTrack;
    int flag0 = 0, flag1 = 0, flag2 = 0;
    String[] directionsList;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_display);
        //   polylines = new ArrayList<>();
        final int[] COLORS = new int[]{R.color.primary_dark_material_light};



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
        //Bundle extras = getIntent().getExtras();
        // number = extras.getString("number");



        panic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:100"));

                if (ActivityCompat.checkSelfPermission(Driver_display1.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
            // dialContactPhone("123123123");
            // startLocationUpdate();

        });

        routes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Direction api comes here
                String start = yourplace.getText().toString();
                String end = destinationplace.getText().toString();


                if (start.matches("") || end.matches("")) {
                    Toast.makeText(getApplicationContext(), "enter the place properly", Toast.LENGTH_SHORT).show();
                } else {


                    mMap.clear();
                    LatLng source = new LatLng(latitude1, longitude1);
                    mMap.addMarker(new MarkerOptions().position(source).title("Your place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    LatLng destination = new LatLng(latitude2, longitude2);
                    mMap.addMarker(new MarkerOptions().position(destination).title("Your destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                    final AlertDialog.Builder alert = new AlertDialog.Builder(Driver_display1.this);
                    final View mView = getLayoutInflater().inflate(R.layout.dialog, null);
                    final EditText way = mView.findViewById(R.id.way);


                    Button bt = mView.findViewById(R.id.waypoints);
                    alert.setView(mView);

                    final AlertDialog alertDialog = alert.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();


                    bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            waypoint = way.getText().toString();
                            CheckBox movement = mView.findViewById(R.id.peopleMovement);
                            CheckBox secured = mView.findViewById(R.id.secured);
                            CheckBox highway = mView.findViewById(R.id.highway);
                            if (secured.isChecked()) {
                                flag0 = 1;
                            }
                            if (movement.isChecked()) {
                                flag1 = 1;
                            }
                            if (highway.isChecked()) {
                                flag2 = 1;
                            }

                            if (flag0 == 1) {
                                String hospital = "hospital";
                                String url1 = getURL(latitude1, latitude2, hospital);
                                Object[] dataTrasfer = new Object[2];
                                dataTrasfer[0] = mMap;
                                dataTrasfer[1] = url1;
                                Driver_display1.myAsycTaskToGetPlaceForDirection myAsycTaskToGetPlace1 = new Driver_display1.myAsycTaskToGetPlaceForDirection();
                                myAsycTaskToGetPlace1.execute(dataTrasfer);

                                String police = "police";
                                String url2 = getURL(latitude1, latitude2, police);
                                Object[] dataTrasfer1 = new Object[2];
                                dataTrasfer1[0] = mMap;
                                dataTrasfer1[1] = url2;
                                Driver_display1.myAsycTaskToGetPlaceForDirectionPolice myAsycTaskToGetPlace2 = new Driver_display1.myAsycTaskToGetPlaceForDirectionPolice();
                                myAsycTaskToGetPlace2.execute(dataTrasfer1);
                            }

                            if (flag1 == 1 & flag2 == 1) {
                                String url = getDirectionUrl();
                                Object[] dataTrasferDirection = new Object[2];
                                dataTrasferDirection[0] = mMap;
                                dataTrasferDirection[1] = url;
                                Driver_display1.GetDirectionsData getDirectionsData = new Driver_display1.GetDirectionsData();
                                getDirectionsData.execute();

                            } else if (flag2 == 1) {
                                StringBuilder urlhere = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude1 + "," + longitude1 + "&" + "destination=" + latitude2 + "," + longitude2 + "&avoid=highways" + "&waypoints=via:" + waypoint + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");

                                String url = urlhere.toString();
                                Object[] dataTrasferDirection = new Object[2];
                                dataTrasferDirection[0] = mMap;
                                dataTrasferDirection[1] = url;
                                Driver_display1.GetDirectionsData getDirectionsData = new Driver_display1.GetDirectionsData();
                                getDirectionsData.execute();

                            } else if (flag1 == 1) {
                                StringBuilder urlhere = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude1 + "," + longitude1 + "&" + "destination=" + latitude2 + "," + longitude2 + "&waypoints=via:" + waypoint + "&departure_time=now" + "&traffic_model=pessimistic" + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");

                                String url = urlhere.toString();
                                Object[] dataTrasferDirection = new Object[2];
                                dataTrasferDirection[0] = mMap;
                                dataTrasferDirection[1] = url;
                                Driver_display1.GetDirectionsData getDirectionsData = new Driver_display1.GetDirectionsData();
                                getDirectionsData.execute();
                            } else {
                                StringBuilder urlhere = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude1 + "," + longitude1 + "&" + "destination=" + latitude2 + "," + longitude2 + "&waypoints=via:" + waypoint + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");

                                String url = urlhere.toString();
                                Object[] dataTrasferDirection = new Object[2];
                                dataTrasferDirection[0] = mMap;
                                dataTrasferDirection[1] = url;
                                Driver_display1.GetDirectionsData getDirectionsData = new Driver_display1.GetDirectionsData();
                                getDirectionsData.execute();

                            }
                            alertDialog.dismiss();
                            flag2 = 0;
                            flag1 = 0;
                            flag0 = 0;
                            startLocationUpdates();

/*


                           Driver_display1.GetDirectionsData getDirectionsData = new Driver_display1.GetDirectionsData();
                           getDirectionsData.execute();
                      // }


             //    waypoint=way.toString();
                Toast.makeText(getApplicationContext(),waypoint,Toast.LENGTH_SHORT).show();


*/


                        }
                    });


                }


            }

        });

        trackMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = yourplace.getText().toString();
                String end = destinationplace.getText().toString();
                LatLng st = new LatLng(latitude1, longitude1);
                LatLng ed = new LatLng(latitude2, longitude2);
                Geocoder myLocation = new Geocoder(Driver_display1.this, Locale.getDefault());
                List<Address> myList = null, myList2 = null;
                try {
                    myList = myLocation.getFromLocation(st.latitude, st.longitude, 1);
                    myList2 = myLocation.getFromLocation(ed.latitude, ed.longitude, 1);
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
                        Driver_display1.GetDirectionsData2 secured = new Driver_display1.GetDirectionsData2();
                        secured.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Only For Chicago", Toast.LENGTH_SHORT).show();
                    }

                }


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
                    startActivityForResult(builder.build(Driver_display1.this), PLACE_PICKER_REQUEST);
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
                    startActivityForResult(builder.build(Driver_display1.this), PLACE_PICKER_REQUEST);
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
                mMap.clear();
                LatLng source = new LatLng(latitude1, longitude1);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 20));
                mMap.addMarker(new MarkerOptions().position(source).title("Your place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(source));
                String hospital = "hospital";
                String url = getURL(latitude1, latitude2, hospital);
                Object[] dataTrasfer = new Object[2];
                dataTrasfer[0] = mMap;
                dataTrasfer[1] = url;
                Driver_display1.myAsycTaskToGetPlace myAsycTaskToGetPlace1 = new myAsycTaskToGetPlace();
                myAsycTaskToGetPlace1.execute(dataTrasfer);

                String police = "police";
                String url1 = getURL(latitude1, latitude2, police);
                Object[] dataTrasfer1 = new Object[2];
                dataTrasfer1[0] = mMap;
                dataTrasfer1[1] = url1;
                Driver_display1.myAsycTaskToGetPlacePolice myAsycTaskToGetPlace2 = new myAsycTaskToGetPlacePolice();
                myAsycTaskToGetPlace2.execute(dataTrasfer1);

            }
        });

    }

    /* private void getRouteToMarker() {

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


     */
    private String getDirectionUrl() {

        StringBuilder urlhere = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude1 + "," + longitude1 + "&" + "destination=" + latitude2 + "," + longitude2 + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        //  StringBuilder urlhere=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return urlhere.toString();
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
                place = PlacePicker.getPlace(Driver_display1.this, data);

                if (a == 0) {
                    toastMsg1 = String.format("%s", place.getName());
                    yourplace.setText(toastMsg1);
                    latitude1 = place.getLatLng().latitude;
                    longitude1 = place.getLatLng().longitude;
                    //   number = place.getPhoneNumber().toString();

                    LatLng source = new LatLng(latitude1, longitude1);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 16));
                    mMap.addMarker(new MarkerOptions().position(source).title("Your place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
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
                    mMap.addMarker(new MarkerOptions().position(destination).title("Your destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
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
        callIntent.setData(Uri.parse(phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PERMISSION_GRANTED) {
        }
        startActivity(callIntent);
    }

   /* @Override
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

    */

    public class AsycTaskToGetPlace extends AsyncTask<Object, String, String> {
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


                }
                count++;
            }

            return nearbyPlaceList;
        }
    }

    // Trigger new location updates at interval
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
        //  Toast.makeText(getApplicationContext(), latLng.toString(), Toast.LENGTH_SHORT).show();
       /* float resultss[]=new float[10];
        for (int k = 0; k < temp.size(); k++) {

            Location.distanceBetween(location.getLatitude(), location.getLongitude(),temp.get(k).longitude,temp.get(k).latitude,resultss);
            if(temp.get(k)!=latLng && resultss[0]>=1000){

                final AlertDialog.Builder alert=new AlertDialog.Builder(Driver_display1.this);
                View mView=getLayoutInflater().inflate(R.layout.alertdialog,null);
               // final EditText way=mView.findViewById(R.id.way);


                alert.setView(mView);

                final AlertDialog alertDialog=alert.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
                break;

            }
            }*/

    }

    protected void startLocationUpdate() {

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
        getFusedLocationProviderClient(Driver_display1.this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChange(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChange(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                location.getLatitude() + "," +
                location.getLongitude();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        String police = "police";
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latLng.latitude + "," + latLng.longitude + "&radius=3000&type=" + police + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");

        String url1 = String.valueOf(googlePlaceUrl);
        Object[] dataTrasfer1 = new Object[2];
        dataTrasfer1[0] = mMap;
        dataTrasfer1[1] = url1;
        Driver_display1.myAsycTaskCallPolice myAsycTaskToGetPlace2 = new myAsycTaskCallPolice();
        myAsycTaskToGetPlace2.execute(dataTrasfer1);


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
                count++;
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
                    mMap.addMarker(new MarkerOptions().position(latlng).title(placeName).icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


                }


            }


            return nearbyPlaceList;
        }
    }


  /*  private List<Updated_route> upRoute=new ArrayList<>();
       public void getSafeRoute(){
          InputStream in= getResources().openRawResource(R.raw.updataed);
           BufferedReader br=new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
           String line=" ";

               try {
                   int a=0;
                   while((line=br.readLine())!=null){
                       String[] tokens=line.split(",");
                       Updated_route route=new Updated_route();
                       route.setLat(tokens[0]);
                       route.setLng(tokens[1]);
                       upRoute.add(route);

                       Log.d("asd", String.valueOf(upRoute));
                       a++;
                       if(a==10) break;
                   }
               } catch (IOException e) {
                   e.printStackTrace();

               }


       }

*/

    public class myAsycTaskToGetPlacePolice extends AsyncTask<Object, String, String> {
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
                count++;
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
                    mMap.addMarker(new MarkerOptions().position(latlng).title(placeName).icon(BitmapDescriptorFactory.fromResource(R.drawable.badge)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


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
                LatLng source = new LatLng(latitude1, longitude1);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 20));


            }


            return nearbyPlaceList;
        }
    }

    public class GetDirectionsData extends AsyncTask<Object, String, String> {
        String url = getDirectionUrl();

        String googleDirectionData;

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
            DataParser parser = new DataParser();
            directionsList = parser.parseDirection(s);
            displayDirection(directionsList);
            LatLng source = new LatLng(latitude1, longitude1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 20));
            //  mMap.addMarker(new MarkerOptions().position(source).title("Your place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(source));

        }

        public void displayDirection(String[] directionList) {
            //int count=directionList.length;
            for (int i = 0; i < directionList.length; i++) {
                PolylineOptions options = new PolylineOptions();
                options.color(Color.RED);
                options.width(10);
                options.addAll(PolyUtil.decode(directionList[i]));
                mMap.addPolyline(options);
                temp = PolyUtil.decode(directionList[i]);
                //for (int k =0; k < temp.size(); k++)
                //Toast.makeText(getApplicationContext(), temp.get(k).toString(), Toast.LENGTH_SHORT).show();
            }


        }


    }

    public class GetDirectionsData2 extends AsyncTask<Object, String, String> {
        String url = getDirectionUrl();

        String googleDirectionData;

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

            DataParser parser = new DataParser();
            directionsList = parser.parseDirection(s);
            displayDirection(directionsList);
            LatLng source = new LatLng(latitude1, longitude1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 20));
            //  mMap.addMarker(new MarkerOptions().position(source).title("Your place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(source));

        }

        public void displayDirection(String[] directionList) {
            for (int i = 0; i < directionList.length; i++) {
//                PolylineOptions options=new PolylineOptions();
//                options.color(Color.RED);
//                options.width(10);
//                options.addAll(PolyUtil.decode(directionList[i]));
//                mMap.addPolyline(options);
                temp = PolyUtil.decode(directionList[i]);
                Driver_display1.securedDirection sec = new Driver_display1.securedDirection();
                sec.execute();
                //for (int k =0; k < temp.size(); k++)
                //Toast.makeText(getApplicationContext(), temp.get(k).toString(), Toast.LENGTH_SHORT).show();
            }
            //  Driver_display1.securedDirection getDirectionsData = new Driver_display1.securedDirection();
            // getDirectionsData.execute();

            //int count=directionList.length;


        }


    }

    public class securedDirection extends AsyncTask<List, String, String> {

        String complete = "no";
        int countSafe = 0;

        @Override
        protected String doInBackground(List... lists) {
            String[] arr = {"first", "second", "third", "forth", "fifth"};
            InputStream[] inputs = new InputStream[5];
            BufferedReader reader1, reader2, reader3, reader4, reader5;
//
//        //startLocationUpdates();
            inputs[0] = getResources().openRawResource(R.raw.first);
            inputs[1] = getResources().openRawResource(R.raw.second);
            inputs[2] = getResources().openRawResource(R.raw.third);
            inputs[3] = getResources().openRawResource(R.raw.fourth);
            inputs[4] = getResources().openRawResource(R.raw.fifth);
            reader1 = new BufferedReader(new InputStreamReader((inputs[0])));
            reader2 = new BufferedReader(new InputStreamReader((inputs[1])));
            reader3 = new BufferedReader(new InputStreamReader((inputs[2])));
            reader4 = new BufferedReader(new InputStreamReader((inputs[3])));
            reader5 = new BufferedReader(new InputStreamReader((inputs[4])));
//        Geocoder myLocation=new Geocoder(Driver_display1.this, Locale.getDefault());
//
//        for (int k =0; k < temp.size(); k++){
//            String[] arr1=temp.get(k).toString().split(" ,");
//            if(temp.get(k).latitude>=46.0 &&temp.get(k).latitude<47.0){
//
//                try {
//                    String csvLine;
//                    int count = 0;
//
//                    while ((csvLine = reader1.readLine()) != null) {
//
//                        String[] row = csvLine.split(",");
//                        if (Double.valueOf(row[0]) <= temp.get(k).latitude) {
//                            LatLng lng = new LatLng(temp.get(k).latitude,temp.get(k).longitude);
//                            Toast.makeText(getApplicationContext(), lng.toString(), Toast.LENGTH_SHORT).show();
//
//
//                            try {
//                                List<Address> myList = myLocation.getFromLocation(lng.latitude, lng.longitude, 1);
//                                Address address = (Address) myList.get(0);
//
//                                myList.get(0).getLocale();
//                                Toast.makeText(getApplicationContext(), myList.get(0).getLocality(), Toast.LENGTH_SHORT).show();
//                            } catch (Exception e) {
//                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                            }
//
//
//                        }
//                    }
//                }
//                catch (Exception e){
//                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
//
//
//                }
//
//
//            }

            //       }


            //String file = "fileoutput";
            //FileOutputStream fw;
            InputStream inputStream = getResources().openRawResource(R.raw.first);
            //  BufferedReader reader = new BufferedReader(new InputStreamReader((inputStream)));
            //PrintWriter writer;

            String csvLine;
            Geocoder myLocation = new Geocoder(Driver_display1.this, Locale.getDefault());

            for (int k = 0; k < temp.size(); k++) {
                DecimalFormat df = new DecimalFormat("#.###");
                String format1 = df.format(temp.get(k).latitude);
                String format2 = df.format(temp.get(k).longitude);

                List<Address> myList1 = null;
                try {
                    myList1 = myLocation.getFromLocation(temp.get(k).latitude, temp.get(k).longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] values = myList1.get(0).getAddressLine(0).split(" ");
                String[] vales = values[0].split(" ");
                // String val=vales[vales.length-2]+" "+vales[vales.length-1];
                if (Double.parseDouble(format1) >= 41.60 & Double.parseDouble(format1) < 41.70) {

                    try {

                        //   fw = openFileOutput(file, MODE_PRIVATE);
                        // writer = new PrintWriter(new OutputStreamWriter(fw));
                        while ((csvLine = reader1.readLine()) != null) {
                            String[] row = csvLine.split(",");
                            LatLng lng = new LatLng(Double.valueOf(row[0]), Double.parseDouble(row[1]));
                            String formatted = df.format(lng.latitude);
                            String formatted2 = df.format(lng.longitude);
                            List<Address> myList = myLocation.getFromLocation(lng.latitude, lng.longitude, 1);
                            String[] values1 = myList.get(0).getAddressLine(0).split(" ");
                            String[] vales1 = values1[0].split(" ");
                            //  String val1=vales[vales.length-2]+" "+vales[vales.length-1];
                            if (vales[0].matches(vales1[0])) {
                                countSafe++;

                            } else if (temp.get(k).latitude < lng.latitude)
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else if (Double.parseDouble(format1) >= 41.70 & Double.parseDouble(format1) < 41.80) {

                    try {

                        //   fw = openFileOutput(file, MODE_PRIVATE);
                        // writer = new PrintWriter(new OutputStreamWriter(fw));
                        while ((csvLine = reader1.readLine()) != null) {
                            String[] row = csvLine.split(",");
                            LatLng lng = new LatLng(Double.valueOf(row[0]), Double.parseDouble(row[1]));
                            String formatted = df.format(lng.latitude);
                            String formatted2 = df.format(lng.longitude);
                            List<Address> myList = myLocation.getFromLocation(lng.latitude, lng.longitude, 1);
                            String[] values1 = myList.get(0).getAddressLine(0).split(" ");
                            String[] vales1 = values1[0].split(" ");
//                        String val1 = vales[vales.length - 2] + " " + vales[vales.length - 1];
                            if (vales[0].matches(vales1[0])) {
                                countSafe++;


                            } else if (temp.get(k).latitude < lng.latitude)
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (Double.parseDouble(format1) >= 41.80 & Double.parseDouble(format1) < 41.90) {

                    try {

                        //     fw = openFileOutput(file, MODE_PRIVATE);
                        //   writer = new PrintWriter(new OutputStreamWriter(fw));
                        while ((csvLine = reader1.readLine()) != null) {
                            String[] row = csvLine.split(",");
                            LatLng lng = new LatLng(Double.valueOf(row[0]), Double.parseDouble(row[1]));
                            String formatted = df.format(lng.latitude);
                            String formatted2 = df.format(lng.longitude);
                            List<Address> myList = myLocation.getFromLocation(lng.latitude, lng.longitude, 1);
                            String[] values1 = myList.get(0).getAddressLine(0).split(" ");
                            String[] vales1 = values1[0].split(" ");
                            //       String val1=vales[vales.length-2]+" "+vales[vales.length-1];
                            if (vales[0].matches(vales1[0])) {
                                countSafe++;

                            } else if (temp.get(k).latitude < lng.latitude)
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else if (Double.parseDouble(format1) >= 41.90 & Double.parseDouble(format1) < 42.0) {

                    try {

                        //  fw = openFileOutput(file, MODE_PRIVATE);
                        //writer = new PrintWriter(new OutputStreamWriter(fw));
                        while ((csvLine = reader1.readLine()) != null) {
                            String[] row = csvLine.split(",");
                            LatLng lng = new LatLng(Double.valueOf(row[0]), Double.parseDouble(row[1]));
                            String formatted = df.format(lng.latitude);
                            String formatted2 = df.format(lng.longitude);
                            List<Address> myList = myLocation.getFromLocation(lng.latitude, lng.longitude, 1);
                            String[] values1 = myList.get(0).getAddressLine(0).split(" ");
                            String[] vales1 = values1[0].split(" ");
                            //       String val1=vales[vales.length-2]+" "+vales[vales.length-1];
                            if (vales[0].matches(vales1[0])) {
                                countSafe++;

                            } else if (temp.get(k).latitude < lng.latitude)
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {

                        //  fw = openFileOutput(file, MODE_PRIVATE);
                        //writer = new PrintWriter(new OutputStreamWriter(fw));
                        while ((csvLine = reader1.readLine()) != null) {
                            String[] row = csvLine.split(",");
                            LatLng lng = new LatLng(Double.valueOf(row[0]), Double.parseDouble(row[1]));
                            String formatted = df.format(lng.latitude);
                            String formatted2 = df.format(lng.longitude);
                            List<Address> myList = myLocation.getFromLocation(lng.latitude, lng.longitude, 1);
                            String[] values1 = myList.get(0).getAddressLine(0).split(" ");
                            String[] vales1 = values1[0].split(" ");
                            //       String val1=vales[vales.length-2]+" "+vales[vales.length-1];
                            if (vales[0].matches(vales1[0])) {
                                countSafe++;

                            } else if (temp.get(k).latitude < lng.latitude)
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


            }
            complete = "yess";
            return null;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            Toast.makeText(getApplicationContext(), count, Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(String s) {

            for (int i = 0; i < directionsList.length; i++) {
                PolylineOptions options = new PolylineOptions();
                options.color(Color.RED);
                options.width(10);
                options.addAll(PolyUtil.decode(directionsList[i]));
                mMap.addPolyline(options);
            }
            Toast.makeText(getApplicationContext(), countSafe + " Total:" + temp.size(), Toast.LENGTH_SHORT).show();


//        @Override
//        protected String doInBackground(List<LatLng> list ) {
//            for (int k =0; k < temp.size(); k++)
//                Toast.makeText(getApplicationContext(), temp.get(k).toString(), Toast.LENGTH_SHORT).show();
//
//
//            return "true";
//        }

        }
    }

    public class myAsycTaskCallPolice extends AsyncTask<Object, String, String> {
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
                count++;
                for (int i = 0; i < nearbyPlaceList.size(); i++) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                    String placeName = googlePlace.get("place_name");
                    String vicinity = googlePlace.get("vicinity");
                    double lat = Double.parseDouble(googlePlace.get("lat"));
                    double lng = Double.parseDouble(googlePlace.get("lng"));

                    LatLng latlng = new LatLng(lat, lng);
                    Geocoder myLocation = new Geocoder(Driver_display1.this, Locale.getDefault());

                    List<Address> myList = null;
                    try {
                        myList = myLocation.getFromLocation(latlng.latitude, latlng.longitude, 1);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


            }


            return nearbyPlaceList;
        }
    }


}

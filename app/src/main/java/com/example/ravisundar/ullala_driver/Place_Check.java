package com.example.ravisundar.ullala_driver;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Place_Check extends AppCompatActivity {
    EditText yourplace;
    Button search;
    TextView Hospital, score, haddress;

    int PLACE_PICKER_REQUEST = 1;
    Double latitude1 = 0.0, longitude1 = 0.0, lat, lng;
    Place place;
    String googlePlaceData, vicinity, placeName, phone = "NA";
    String url;
    Geocoder geocoder;
    List<Address> addresses = new ArrayList<Address>();
    HashMap<String, String> googlePlace;
    int numberofplaces = 0;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place__check);
        yourplace = findViewById(R.id.place);
        search = findViewById(R.id.search);

        Hospital = findViewById(R.id.hospital);
        score = findViewById(R.id.paddress);
        haddress = findViewById(R.id.hadress);


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hospital.setText(" ");
                haddress.setText(" ");
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Place_Check.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    String x = e.toString();
                }
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(Place_Check.this, data);

                String toastMsg = String.format("%s", place.getName());
                yourplace.setText(toastMsg);

                String placetype = "hospital";
                latitude1 = place.getLatLng().latitude;
                longitude1 = place.getLatLng().longitude;
                Toast.makeText(this, latitude1 + " " + longitude1, Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), (int) (latitude1+longitude1), Toast.LENGTH_SHORT).show();
                String url = getURL(latitude1, longitude1, placetype);
                Object[] dataTrasfer = new Object[2];
                dataTrasfer[0] = mMap;
                dataTrasfer[1] = url;
                MyAsycTaskToGetPlace myAsycTaskToGetPlace = new MyAsycTaskToGetPlace();
                myAsycTaskToGetPlace.execute(dataTrasfer);


            }
        }

    }

    private List<HashMap<String, String>> showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
        numberofplaces = nearbyPlaceList.size();

        for (int i = 0; i < nearbyPlaceList.size(); i++) {
            googlePlace = nearbyPlaceList.get(i);

            placeName = googlePlace.get("place_name");
            vicinity = googlePlace.get("vicinity");
            lat = Double.parseDouble(googlePlace.get("lat"));
            lng = Double.parseDouble(googlePlace.get("lng"));
//            geocoder=new Geocoder(this);
//
//            try {
//                addresses=geocoder.getFromLocation(lat,lng,1);
//                phone=addresses.get(0).getPhone();
//                Log.e("as",vicinity);
//                Toast.makeText(getApplicationContext(), phone, Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                if (geocoder.isPresent()) Log.e("asddsa","Present");
//                Log.e("adsasd",e.toString());
//            }
//            finally {
//                Log.e("dhgfa",vicinity);
//            }
            break;
        }

        return nearbyPlaceList;
    }

    private String getURL(Double latitude1, Double latitude2, String nearbyplace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude1 + "," + longitude1 + "&radius=1500&type=" + nearbyplace + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return googlePlaceUrl.toString();

    }

    public class MyAsycTaskToGetPlace extends AsyncTask<Object, String, String> {

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
            Hospital.setText(placeName);
            haddress.setText(vicinity);


        }

    }

}

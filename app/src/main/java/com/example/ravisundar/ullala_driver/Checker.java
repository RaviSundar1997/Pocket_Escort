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

public class Checker extends AppCompatActivity {
    EditText yourplace;
    Button search;
    TextView Hospital, score, haddress, police, paddress;

    int PLACE_PICKER_REQUEST = 1;
    Double latitude1 = 0.0, longitude1 = 0.0, lat, lng;
    Place place;
    String googlePlaceData, phone = "NA";
    String url;
    Geocoder geocoder;
    List<Address> addresses = new ArrayList<Address>();
    HashMap<String, String> googlePlace;
    int numberofplaces = 0;
    String[] arr = new String[2];
    Object[] dataTrasfer = new Object[2];
    int i = 0, count = 0;
    private GoogleMap mMap;

    public Object[] che() {
        return dataTrasfer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checker);
        yourplace = findViewById(R.id.place);
        search = findViewById(R.id.search);

        Hospital = findViewById(R.id.hospital);
        score = findViewById(R.id.score);
        haddress = findViewById(R.id.hadress);
        police = findViewById(R.id.Police);
        paddress = findViewById(R.id.padress);


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hospital.setText(" ");
                haddress.setText(" ");
                police.setText(" ");
                paddress.setText(" ");
                arr[0] = "hospital";
                arr[1] = "police";
                i = 0;
                count = 0;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Checker.this), PLACE_PICKER_REQUEST);
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
                place = PlacePicker.getPlace(Checker.this, data);
                String toastMsg = String.format("%s", place.getName());
                yourplace.setText(toastMsg);
                latitude1 = place.getLatLng().latitude;
                longitude1 = place.getLatLng().longitude;
                Toast.makeText(this, latitude1 + " " + longitude1, Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), (int) (latitude1+longitude1), Toast.LENGTH_SHORT).show();
                String url = getURL(latitude1, longitude1, arr[0]);
                Object[] dataTrasfer = new Object[2];
                dataTrasfer[0] = mMap;
                dataTrasfer[1] = url;
                Checker.MyAsycTaskToGetPlace myAsycTaskToGetPlace = new Checker.MyAsycTaskToGetPlace();
                myAsycTaskToGetPlace.execute(dataTrasfer);

            }
        }

    }

    private List<HashMap<String, String>> showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
        numberofplaces = nearbyPlaceList.size();

        for (int j = 0; j < nearbyPlaceList.size(); j++) {
            googlePlace = nearbyPlaceList.get(j);
            String vicinity, placeName;
            placeName = googlePlace.get("place_name");
            vicinity = googlePlace.get("vicinity");
            if (!placeName.isEmpty()) {
                if (i == 0) {
                    count += 1;
                    Hospital.setText(placeName);
                    haddress.setText(vicinity);
                    i += 1;
                } else {

                    police.setText(placeName);
                    paddress.setText(vicinity);
                    count += 1;
                }
                lat = Double.parseDouble(googlePlace.get("lat"));
                lng = Double.parseDouble(googlePlace.get("lng"));
                break;
            }
        }

        return nearbyPlaceList;
    }


    private String getURL(Double latitude1, Double latitude2, String nearbyplace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude1 + "," + longitude1 + "&radius=1500&type=" + nearbyplace + "&key=AIzaSyC_fM5v5r7Y-NIIyGM2UL6xMuOR_TlJyuQ");
        return googlePlaceUrl.toString();

    }

    private String toDownloadUrl(String ur) {
        url = ur;
        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlaceData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return googlePlaceData;

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

            if (i == 1) {
                String url = getURL(latitude1, longitude1, arr[1]);
                Object[] dataTrasfer = new Object[2];
                dataTrasfer[0] = mMap;
                dataTrasfer[1] = url;
                Checker.MyAsycTaskToGetPlace myAsycTaskToGetPlace = new Checker.MyAsycTaskToGetPlace();
                myAsycTaskToGetPlace.execute(dataTrasfer);


            }

            if (count == 0)
                score.setText("POOR");
            else if (count == 1)
                score.setText("GOOD");
            else
                score.setText("SECURE");


        }

    }


}

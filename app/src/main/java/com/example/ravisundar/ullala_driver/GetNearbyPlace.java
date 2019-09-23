package com.example.ravisundar.ullala_driver;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlace extends AsyncTask<Object, String, String> {
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

        return nearbyPlaceList;
    }
}

package com.example.ravisundar.ullala_driver;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class gePlace extends AsyncTask<Object, String, String> {
    GoogleMap mMap;
    String url;
    List<HashMap<String, String>> nearbyplaceList = null;
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

        DataParser parser = new DataParser();
        nearbyplaceList = parser.parse(s);


    }

    public String getPlaces() {
        return nearbyplaceList.toString();
    }

}

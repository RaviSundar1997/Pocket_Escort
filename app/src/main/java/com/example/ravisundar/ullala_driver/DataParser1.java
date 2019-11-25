package com.example.ravisundar.ullala_driver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser1 {
    ArrayList<String> arrayList = new ArrayList<String>();

    private HashMap<String, String> getplace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePLacesMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String lat = "";
        String lng = " ";
        String ref = " ";
        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
                arrayList.add(placeName);


            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");

            }
            lng = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            lat = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            ref = googlePlaceJson.getString("reference");

            googlePLacesMap.put("place_name", placeName);
            googlePLacesMap.put("vicinity", vicinity);
            googlePLacesMap.put("lat", lat);
            googlePLacesMap.put("lng", lng);
            googlePLacesMap.put("reference", ref);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return googlePLacesMap;

    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placMap = null;
        for (int i = 0; i < count; i++) {
            try {
                placMap = getplace((JSONObject) jsonArray.get(i));
                placesList.add(placMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;


    }

    public List<HashMap<String, String>> parse(String jsonData) {

        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }
}

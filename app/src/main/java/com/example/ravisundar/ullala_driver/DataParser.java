package com.example.ravisundar.ullala_driver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {


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

    public String[] parseDirection(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPaths(jsonArray);
    }

    public String[] getPaths(JSONArray googleStepsJson) {
        int count = googleStepsJson.length();
        String[] polylines = new String[count];
        for (int i = 0; i < count; i++) {
            try {
                polylines[i] = getpath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    public String getpath(JSONObject googlePathJson) {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return polyline;
    }
}

package com.example.ravisundar.ullala_driver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Routesclass {

    int size;
    ArrayList<String> arrayList = new ArrayList<String>();

    public String[] parseDirection(String jsonData, int val) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            size = jsonObject.getJSONArray("routes").length();
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(val).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

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

    public int routeSize() {
        return size;
    }
}

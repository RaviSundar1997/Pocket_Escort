package com.example.ravisundar.ullala_driver;

import java.io.IOException;

public class CheckerUrl {
    Checker checker = new Checker();
    Object[] data = new Object[2];
    String googlePlaceData, url;

    void eg() {
        data = checker.che();
    }

    public String DoingInBackGround() {
        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            url = (String) data[1];
            googlePlaceData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlaceData;
    }
}

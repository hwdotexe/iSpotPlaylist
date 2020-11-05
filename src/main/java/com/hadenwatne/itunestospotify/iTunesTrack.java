package com.hadenwatne.itunestospotify;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class iTunesTrack {
    private String trackName;
    private String trackArtist;
    private String trackAlbum;
    private int trackID;

    public iTunesTrack(String name, String artist, String album, int id) {
        this.trackName = name;
        this.trackArtist = artist;
        this.trackAlbum = album;
        this.trackID = id;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public String getTrackAlbum() {
        return trackAlbum;
    }

    public String getTrackArtist() {
        return trackArtist;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();

        obj.put("name", this.trackName);
        obj.put("artist", this.trackArtist);
        obj.put("album", this.trackAlbum);
        obj.put("id", this.trackID);

        return obj;
    }
}

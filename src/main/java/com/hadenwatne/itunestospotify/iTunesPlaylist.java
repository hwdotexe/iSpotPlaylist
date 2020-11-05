package com.hadenwatne.itunestospotify;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class iTunesPlaylist {
    private String playlistName;
    private List<Integer> tracks;

    public iTunesPlaylist(String name) {
        this.playlistName = name;
        this.tracks = new ArrayList<>();
    }

    public void addTrack(int trackInteger) {
        this.tracks.add(trackInteger);
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        JSONArray tracks = new JSONArray();

        for(int t : this.tracks) {
            tracks.put(t);
        }

        obj.put("name", sanitizeName());
        obj.put("tracks", tracks);

        return obj;
    }

    private String sanitizeName() {
        return this.playlistName.replaceAll("[^a-zA-Z\\d\\s\\w]", "");
    }
}

package com.hadenwatne.itunestospotify.spotifyauth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Spotify {
    private static String token;
    private static String userID;
    private static String playlistID;

    public static void setToken(String t) {
        token = t;
    }

    private static void setUserID() {
        String response = HTTPUtil.SendGET("https://api.spotify.com/v1/me", token);

        if(response != null) {
            JSONObject r = new JSONObject(response);

            userID = r.getString("id");
        } else {
            System.out.println("=== Could not get User information from Spotify ===");
        }
    }

    public static String SearchForSong(String title, String artist) {
        String q = HTTPUtil.URLEncode("track:"+title+" artist:"+artist+"&type=track");
        String response = HTTPUtil.SendGET("https://api.spotify.com/v1/search?q="+q, token);

        if(response != null) {
            JSONArray r = new JSONObject(response).getJSONObject("tracks").getJSONArray("items");

            if(r.length() > 0) {
                return r.getJSONObject(0).getString("uri");
            } else {
                System.out.println("No results for song: "+title+" by "+artist);
                return null;
            }
        } else {
            System.out.println("=== Could not get song information from Spotify ===");
            return null;
        }
    }

    public static void CreatePlaylist(String name) {
        if(userID == null)
            setUserID();

        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("public", false);

        String response = HTTPUtil.SendPOST("https://api.spotify.com/v1/users/"+userID+"/playlists", token, data.toString());

        if(response != null) {
            JSONObject r = new JSONObject(response);

            playlistID = r.getString("id");
        } else {
            System.out.println("=== Could not create playlist on Spotify ===");
        }
    }

    public static void AddToPlaylist(List<String> uris) {
        JSONObject main = new JSONObject();
        JSONArray body = new JSONArray();

        for(String uri : uris) {
            body.put(uri);
        }

        main.put("uris", body);

        String response = HTTPUtil.SendPOST("https://api.spotify.com/v1/playlists/"+playlistID+"/tracks", token, main.toString());

        if(response != null) {
            JSONObject r = new JSONObject(response);

            if(!r.has("snapshot_id")){
                System.out.println("Could not add tracks to Spotify :(");
            }
        } else {
            System.out.println("=== Could not get User information from Spotify ===");
        }
    }
}

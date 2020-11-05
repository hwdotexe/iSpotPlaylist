package com.hadenwatne.itunestospotify;

import com.hadenwatne.itunestospotify.models.iTunesPlaylist;
import com.hadenwatne.itunestospotify.models.iTunesTrack;
import com.hadenwatne.itunestospotify.spotify.Spotify;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class iSpotPlaylist {
    public static void main(String[] args) {
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "convert":
                    if(args.length == 3) {
                        JSONObject converted = convert(args[1]);

                        Utils.writeFile(args[2], converted.toString());
                    }else{
                        System.out.println("Invalid arguments! Please supply an input XML file, and an output JSON file.");
                    }
                    break;
                case "upload":
                    if(args.length == 2) {
                        String data = Utils.readFile(args[1]);

                        if(data != null) {
                            upload(new JSONObject(data));
                        }else{
                            System.out.println("There was an error reading the file you supplied. Please run the 'convert' function first.");
                        }
                    }else{
                        System.out.println("Invalid arguments! Please supply an input XML file, and an output JSON file.");
                    }
                    break;
                case "process":
                    if(args.length == 2) {
                        JSONObject converted = convert(args[1]);

                        upload(converted);
                    }else{
                        System.out.println("Invalid arguments! Please supply an input XML file, and an output JSON file.");
                    }
                    break;
                default:
                    System.out.println("Unrecognized command!");
            }
        }
    }

    private static JSONObject convert(String input) {
        String xml = Utils.readFile(input);

        return Utils.ConvertITunesXMLToJSON(xml);
    }

    private static void upload(JSONObject json) {
        List<iTunesPlaylist> playlists = Utils.loadPlaylistsFromJSON(json.getJSONArray("playlists"));
        List<iTunesTrack> tracks = Utils.loadTracksFromJSON(json.getJSONArray("tracks"));

        System.out.println(">> Please paste your Spotify access token:");
        String accessToken = System.console().readLine();

        Spotify.setToken(accessToken);

        String playlistList = Utils.buildPlaylistList(playlists);

        while(true) {
            System.out.println("\n== Your current playlists: ==\n" + playlistList);

            System.out.println("\n>> Type the name of the iTunes playlist you want to upload to Spotify, or type \"exit\" to finish:");
            String playlistToUpload = System.console().readLine();

            if (playlistToUpload.equalsIgnoreCase("exit")) {
                break;
            }

            boolean found = false;
            for(iTunesPlaylist pl : playlists) {
                if(pl.getPlaylistName().equalsIgnoreCase(playlistToUpload)) {
                    List<iTunesTrack> tracksInPlaylist = Utils.getTracksInPlaylist(tracks, pl);

                    Utils.performSpotifyUpload(pl.getPlaylistName(), tracksInPlaylist);

                    System.out.println("\n\n================\n=== COMPLETE ===\n================\n\n");

                    found = true;
                    break;
                }
            }

            if(!found) {
                System.out.println("\n[!] Playlist " + playlistToUpload + " not found.");
            }
        }
    }
}

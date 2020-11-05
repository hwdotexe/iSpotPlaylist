package com.hadenwatne.itunestospotify;

import com.hadenwatne.itunestospotify.spotifyauth.Spotify;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ITTS {
    public static void main(String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("convert")) {
                if(args.length == 3) {
                    JSONObject convertedJSON = convertXMLToJSON(args[1]);

                    writeFile(args[2], convertedJSON.toString());

                    System.out.println("iTunes XML converted to JSON!");
                }else{
                    System.out.println("Incorrect number of arguments.");
                }
            } else if(args[0].equalsIgnoreCase("upload")) {
                if(args.length == 2) {
                    String data = readFile(args[1]);

                    uploadPlaylistToSpotify(data);
                }else{
                    System.out.println("Incorrect number of arguments.");
                }
            } else if(args[0].equalsIgnoreCase("process")) {
                if(args.length == 2) {
                    JSONObject convertedJSON = convertXMLToJSON(args[1]);

                    uploadPlaylistToSpotify(convertedJSON.toString());
                }else{
                    System.out.println("Incorrect number of arguments.");
                }
            } else {
                System.out.println("Unrecognized argument!");
            }
        } else {
            System.out.println("Valid arguments are:\n" +
                    "process <iTunes XML file location>\n" +
                    "convert <iTunes XML file location> <output JSON file location>\n" +
                    "upload <converted library JSON file location>");
        }
    }

    private static void uploadPlaylistToSpotify(String data) {
        JSONObject jo = new JSONObject(data);
        JSONArray playlists = jo.getJSONArray("playlists");
        JSONArray tracks = jo.getJSONArray("tracks");

        System.out.println(">> Please paste your Spotify access token:");
        String accessToken = System.console().readLine();

        Spotify.setToken(accessToken);

        // Get a list of iTunes Playlists found in JSON, and show it to the user.
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<playlists.length(); i++) {
            JSONObject temp = playlists.getJSONObject(i);

            if(sb.length() > 0) {
                sb.append("\n");
            }

            sb.append(temp.getString("name"));
        }

        sb.append("\n=============");

        while(true) {
            System.out.println("== Your current playlists: ==\n" + sb.toString());

            System.out.println(">> Type the name of the iTunes playlist you want to upload to Spotify, or type \"exit\" to finish:");
            String playlistToUpload = System.console().readLine();

            if(playlistToUpload.equalsIgnoreCase("exit")) {
                break;
            }

            if(!sb.toString().toLowerCase().contains(playlistToUpload.toLowerCase())) {
                System.out.println("\n[!] Playlist not found.\n");
                continue;
            }

            for (int p = 0; p < playlists.length(); p++) {
                JSONObject playlistObject = playlists.getJSONObject(p);

                if (playlistObject.getString("name").equalsIgnoreCase(playlistToUpload)) {
                    JSONArray playlistTracks = playlistObject.getJSONArray("tracks");
                    List<Integer> playlistTrackIDs = new ArrayList<>();

                    for (int t = 0; t < playlistTracks.length(); t++) {
                        playlistTrackIDs.add(playlistTracks.getInt(t));
                    }

                    List<iTunesTrack> newTracks = new ArrayList<>();

                    for (int song : playlistTrackIDs) {
                        for (int t = 0; t < tracks.length(); t++) {
                            JSONObject playlistTrack = tracks.getJSONObject(t);

                            if (playlistTrack.getInt("id") == song) {
                                // Get the name of the song, and strip out any odd characters (because URI). Spotify will re-match the song title later.
                                String trackName = playlistTrack.getString("name");
                                Matcher m = Pattern.compile("^([a-z\\s\\d',.()]+)", Pattern.CASE_INSENSITIVE).matcher(trackName);

                                if (m.find()) {
                                    newTracks.add(new iTunesTrack(m.group(1).trim(), playlistTrack.getString("artist"), playlistTrack.getString("album"), song));
                                }

                                break;
                            }
                        }
                    }

                    // Create the playlist
                    Spotify.CreatePlaylist(playlistObject.getString("name"));
                    System.out.println("Created the playlist!");

                    System.out.println("Adding songs...");
                    // Search for the songs
                    List<String> songURIs = new ArrayList<>();

                    for (iTunesTrack track : newTracks) {
                        String songID = Spotify.SearchForSong(track.getTrackName(), track.getTrackArtist());

                        if(songID != null) {
                            songURIs.add(songID);
                        }
                    }

                    // Add the songs to the playlist.
                    Spotify.AddToPlaylist(songURIs);

                    System.out.println("\n\n======= Complete! =======\n\n");

                    break;
                }
            }
        }
    }

    private static JSONObject convertXMLToJSON(String inputFile) {
        List<iTunesPlaylist> iTunesPlaylists = new ArrayList<>();
        List<iTunesTrack> iTunesTracks = new ArrayList<>();

        JSONObject jsonInput = XML.toJSONObject(readFile(inputFile));
        JSONArray playlistsInput = jsonInput.getJSONObject("plist").getJSONObject("dict").getJSONObject("array").getJSONArray("dict");
        JSONArray tracksInput = jsonInput.getJSONObject("plist").getJSONObject("dict").getJSONObject("dict").getJSONArray("dict");

        for(int i=0; i<playlistsInput.length(); i++) {
            JSONObject jo = playlistsInput.getJSONObject(i);
            String name = jo.getJSONArray("string").getString(1);

            if(jo.has("array")) {
                JSONArray playlistTracks = jo.getJSONObject("array").getJSONArray("dict");
                iTunesPlaylist pl = new iTunesPlaylist(name);

                for (int t = 0; t < playlistTracks.length(); t++) {
                    int trackNum = playlistTracks.getJSONObject(t).getInt("integer");

                    pl.addTrack(trackNum);
                }

                iTunesPlaylists.add(pl);
            }
        }

        for(int i=0; i<tracksInput.length(); i++) {
            JSONObject jo = tracksInput.getJSONObject(i);
            JSONArray strings = jo.getJSONArray("string");

            if(strings.length() >= 4) {
                String trackName = strings.get(2).toString();
                String trackArtist = strings.getString(3);
                String trackAlbum = strings.getString(6);
                int trackID = jo.getJSONArray("integer").getInt(0);
                iTunesTrack track = new iTunesTrack(trackName, trackArtist, trackAlbum, trackID);

                iTunesTracks.add(track);
            }
        }

        JSONObject out = new JSONObject();
        JSONArray playlistsOut = new JSONArray();
        JSONArray tracksOut = new JSONArray();

        for(iTunesPlaylist pl : iTunesPlaylists) {
            playlistsOut.put(pl.toJson());
        }

        for(iTunesTrack tr : iTunesTracks) {
            tracksOut.put(tr.toJson());
        }

        out.put("playlists", playlistsOut);
        out.put("tracks", tracksOut);

        return out;
    }

    private static void writeFile(String file, String data) {
        byte[] bytes = data.getBytes();

        try {
            File bf = new File(file);
            FileOutputStream os = new FileOutputStream(bf);

            if(!bf.exists())
                bf.createNewFile();

            os.write(bytes);
            os.flush();
            os.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String path){
        try {
            int data;
            FileInputStream is = new FileInputStream(path);
            StringBuilder xmlData = new StringBuilder();

            while ((data = is.read()) != -1) {
                xmlData.append((char) data);
            }

            is.close();

            return xmlData.toString();
        }catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

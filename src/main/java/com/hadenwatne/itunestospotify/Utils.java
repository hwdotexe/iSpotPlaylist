package com.hadenwatne.itunestospotify;

import com.hadenwatne.itunestospotify.models.iTunesPlaylist;
import com.hadenwatne.itunestospotify.models.iTunesTrack;
import com.hadenwatne.itunestospotify.spotify.Spotify;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static JSONObject ConvertITunesXMLToJSON(String xml) {
        List<iTunesPlaylist> iTunesPlaylists = new ArrayList<>();
        List<iTunesTrack> iTunesTracks = new ArrayList<>();

        JSONObject jsonInput = XML.toJSONObject(xml);
        JSONArray playlistsInput = jsonInput.getJSONObject("plist").getJSONObject("dict").getJSONObject("array").getJSONArray("dict");
        JSONArray tracksInput = jsonInput.getJSONObject("plist").getJSONObject("dict").getJSONObject("dict").getJSONArray("dict");

        for(int i=0; i<playlistsInput.length(); i++) {
            JSONObject root = playlistsInput.getJSONObject(i);
            iTunesPlaylist playlist = modelPlaylistData(root);

            if(playlist != null) {
                iTunesPlaylists.add(playlist);
            }
        }

        for(int i=0; i<tracksInput.length(); i++) {
            JSONObject root = tracksInput.getJSONObject(i);
            iTunesTrack track = modelITunesTracks(root);

            if(track != null) {
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

    private static iTunesTrack modelITunesTracks(JSONObject tracksRoot) {
        JSONArray trackStrings = tracksRoot.getJSONArray("string");

        if(trackStrings.length() >= 7) {
            String trackName = trackStrings.get(2).toString();
            String trackArtist = trackStrings.getString(3);
            String trackAlbum = trackStrings.getString(6);
            int trackID = tracksRoot.getJSONArray("integer").getInt(0);

            return new iTunesTrack(trackName, trackArtist, trackAlbum, trackID);
        }

        return null;
    }

    private static iTunesPlaylist modelPlaylistData(JSONObject playlistRoot) {
        String name = playlistRoot.getJSONArray("string").getString(1);

        if(playlistRoot.has("array")) {
            JSONArray playlistTracks = playlistRoot.getJSONObject("array").getJSONArray("dict");
            iTunesPlaylist pl = new iTunesPlaylist(name);

            for (int t = 0; t < playlistTracks.length(); t++) {
                int trackNum = playlistTracks.getJSONObject(t).getInt("integer");

                pl.addTrack(trackNum);
            }

            return pl;
        }

        return null;
    }

    public static List<iTunesPlaylist> loadPlaylistsFromJSON(JSONArray playlists) {
        List<iTunesPlaylist> iTunesPlaylists = new ArrayList<>();

        for(int i=0; i<playlists.length(); i++) {
            JSONObject temp = playlists.getJSONObject(i);
            JSONArray tracks = temp.getJSONArray("tracks");
            iTunesPlaylist playlist = new iTunesPlaylist(temp.getString("name"));

            for(int t=0; t<tracks.length(); t++) {
                playlist.addTrack(tracks.getInt(t));
            }

            iTunesPlaylists.add(playlist);
        }

        return iTunesPlaylists;
    }

    public static List<iTunesTrack> loadTracksFromJSON(JSONArray tracks) {
        List<iTunesTrack> iTunesTracks = new ArrayList<>();

        for(int i=0; i<tracks.length(); i++) {
            JSONObject temp = tracks.getJSONObject(i);
            iTunesTrack track = new iTunesTrack(temp.getString("name"), temp.getString("artist"), temp.getString("album"), temp.getInt("id"));

            iTunesTracks.add(track);
        }

        return iTunesTracks;
    }

    public static List<iTunesTrack> getTracksInPlaylist(List<iTunesTrack> tracks, iTunesPlaylist playlist) {
        List<iTunesTrack> iTunesTracks = new ArrayList<>();

        for(int i : playlist.getTracks()) {
            for(iTunesTrack t : tracks) {
                if(t.getTrackID() == i) {
                    iTunesTracks.add(t);
                    break;
                }
            }
        }

        return iTunesTracks;
    }

    public static String buildPlaylistList(List<iTunesPlaylist> playlists) {
        StringBuilder sb = new StringBuilder();

        for(iTunesPlaylist pl : playlists) {
            if(sb.length() > 0) {
                sb.append("\n");
            }

            sb.append(playlists.indexOf(pl)+1);
            sb.append(". ");
            sb.append(pl.getPlaylistName());
        }

        return sb.toString();
    }

    public static void performSpotifyUpload(String playlistName, List<iTunesTrack> tracks) {
        Spotify.CreatePlaylist(playlistName);

        List<String> songURIs = new ArrayList<>();

        for (iTunesTrack track : tracks) {
            String songID = Spotify.SearchForSong(track.getTrackName(), track.getTrackArtist());

            if(songID != null) {
                songURIs.add(songID);
            }
        }

        Spotify.AddToPlaylist(songURIs);
    }

    public static void writeFile(String file, String data) {
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

    public static String readFile(String path){
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

            return null;
        }
    }
}

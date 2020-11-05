package com.hadenwatne.itunestospotify.spotifyauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HTTPUtil {
    public static String SendGET(String URI, String token) {
        try {
            URL url = new URL(URI);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            if(token != null) {
                conn.setRequestProperty("Authorization", "Bearer "+token);
            }

            // Retrieve data
            if (conn.getResponseCode() < 300) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                rd.close();
                conn.disconnect();

                return result.toString();
            } else {
                System.out.println("Received "+conn.getResponseCode()+": "+conn.getResponseMessage());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String SendPOST(String URI, String token, String body) {
        try {
            URL url = new URL(URI);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");

            if(token != null) {
                conn.setRequestProperty("Authorization", "Bearer "+token);
            }

            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/json");
            conn.getOutputStream().write(body.getBytes());

            // Retrieve data
            if (conn.getResponseCode() < 300) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                rd.close();
                conn.disconnect();

                return result.toString();
            } else {
                System.out.println("Received "+conn.getResponseCode()+": "+conn.getResponseMessage());

                System.out.println("URI:"+URI);
                System.out.println("Body:"+body);
                System.out.println("Result:");

                // TESTING TESTING TESTING TESTING TESTING
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                rd.close();
                conn.disconnect();
                // TESTING TESTING TESTING TESTING TESTING

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String URLEncode(String text) {
        try {
            return text.replaceAll("\\s+", "%20").replaceAll(":", "%3A").replaceAll("[^a-zA-Z\\s0-9().&=,'%]", "");
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}

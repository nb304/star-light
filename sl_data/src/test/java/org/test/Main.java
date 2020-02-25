package org.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Main {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://39.105.41.2:9000/king2-product-image/gg.txt");
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(20000);
        urlConnection.setReadTimeout(20000);
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "gbk"));
        String read = "";
        String readStr = "";
        while ((read = br.readLine()) != null) {
            readStr = readStr + read;
        }
        br.close();
        System.out.println(readStr);
    }
}

package com.test.pageloaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Simple web page loader that just opens a URL and reads all the text from it
 */
public class SimplePageLoader implements PageLoader {
    public String loadPage(String urlpath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new URL(urlpath)
                                .openConnection()
                                .getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        bufferedReader.close();

        return sb.toString();
    }

    public void cleanup() { }
}

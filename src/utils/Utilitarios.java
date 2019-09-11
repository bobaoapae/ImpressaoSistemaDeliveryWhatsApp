package utils;

import adapters.*;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class Utilitarios {
    public static GsonBuilder getDefaultGsonBuilder(Type type) {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping().
                registerTypeAdapter(LocalTime.class, new LocalTimeAdapterSerialize()).
                registerTypeAdapter(LocalTime.class, new LocalTimeAdapterDeserialize()).
                registerTypeAdapter(LocalDate.class, new LocalDateAdapterSerialize()).
                registerTypeAdapter(LocalDate.class, new LocalDateAdapterDeserialize()).
                registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapterSerialize()).
                registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapterDeserialize()).
                registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapterSerialize()).
                registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapterDeserialize());
        return builder;
    }

    public static String getText(String url) {
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static int getResponseCode(String url) {
        try {
            URL website = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) website.openConnection();
            connection.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseCode();
        } catch (Exception ex) {
            return -1;
        }
    }
}

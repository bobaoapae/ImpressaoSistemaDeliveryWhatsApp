package utils;

import adapters.*;
import com.google.gson.GsonBuilder;
import modelo.ItemPedido;
import modelo.Produto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Utilitarios {
    public static GsonBuilder getDefaultGsonBuilder(Type type) {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping().
                registerTypeAdapter(java.sql.Date.class, new DateAdapterSerialize()).
                registerTypeAdapter(java.sql.Date.class, new DateAdapterDeserialize()).
                registerTypeAdapter(Timestamp.class, new TimestampAdapterSerialize()).
                registerTypeAdapter(Timestamp.class, new TimestampAdapterDeserialize()).
                registerTypeAdapter(Time.class, new TimeAdapter()).
                registerTypeAdapter(Time.class, new TimeAdapterDeserialize());
        HashMap<Type, Object> adapters = new HashMap<>();
        adapters.put(ItemPedido.class, new UseGetterAdapterSerialize<>());
        adapters.put(Produto.class, new UseGetterAdapterSerialize<>());
        for (Map.Entry<Type, Object> entry : adapters.entrySet()) {
            if (entry.getKey().equals(type)) {
                continue;
            }
            builder.registerTypeAdapter(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public static String getText(String url) {
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

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

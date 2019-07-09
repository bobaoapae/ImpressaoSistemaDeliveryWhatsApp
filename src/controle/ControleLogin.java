package controle;

import com.google.gson.JsonParser;
import modelo.Usuario;
import utils.Utilitarios;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class ControleLogin {
    private String endPoint;
    private static ControleLogin instance;

    private ControleLogin() {
        if (new File("homologa.zapia").exists()) {
            this.endPoint = "http://zapia.com.br:8081";
        } else {
            this.endPoint = "http://zapia.com.br:8080";
        }
    }

    public Usuario getUsuario(String usuario, String senha) {
        String json = "";
        try {
            json = Utilitarios.getText("" + endPoint + "/manager/login?login=" + URLEncoder.encode(usuario, "UTF-8") + "&senha=" + URLEncoder.encode(senha, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (json.isEmpty()) {
            return null;
        }
        return Utilitarios.getDefaultGsonBuilder(null).create().fromJson(json, Usuario.class);
    }

    public String getToken(String usuario, String senha, UUID uuid) {
        try {
            String json = Utilitarios.getText("" + endPoint + "/manager/generateToken?login=" + URLEncoder.encode(usuario, "UTF-8") + "&senha=" + URLEncoder.encode(senha, "UTF-8") + "&estabelecimento=" + URLEncoder.encode(uuid.toString(), "UTF-8"));
            if (json.isEmpty()) {
                return "";
            }
            JsonParser parser = new JsonParser();
            return parser.parse(json).getAsJsonObject().get("token").getAsString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static ControleLogin getInstance() {
        if (instance == null) {
            instance = new ControleLogin();
        }
        return instance;
    }
}

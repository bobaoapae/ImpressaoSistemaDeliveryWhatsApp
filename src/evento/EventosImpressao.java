package evento;

import com.google.gson.Gson;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import modelo.Pedido;
import modelo.Reserva;
import utils.Utilitarios;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EventosImpressao implements EventHandler {

    private ActionOnNovaReserva actionOnNovaReserva;
    private ActionOnNovoPedido actionOnNovoPedido;
    private ActionOnError actionOnError;
    private ActionOnUpdatePedido actionOnUpdatePedido;
    private Runnable disconnect, open, onLogout;
    private Gson builder;
    private String token;
    private EventSource source;
    private boolean aleardyOpen = false;
    private ActionOnAjuda actionOnAjuda;
    private String endPoint;

    public EventosImpressao(String token, ActionOnNovoPedido actionOnNovoPedido, ActionOnUpdatePedido actionOnUpdatePedido, ActionOnNovaReserva actionOnNovaReserva, ActionOnAjuda actionOnAjuda, Runnable open, Runnable disconnect, ActionOnError actionOnError, Runnable onLogout) {
        this.actionOnNovoPedido = actionOnNovoPedido;
        this.actionOnUpdatePedido = actionOnUpdatePedido;
        this.actionOnNovaReserva = actionOnNovaReserva;
        this.actionOnAjuda = actionOnAjuda;
        this.token = token;
        this.open = open;
        this.actionOnError = actionOnError;
        this.disconnect = disconnect;
        this.onLogout = onLogout;
        builder = Utilitarios.getDefaultGsonBuilder(null).create();
        if (new File("homologa.zapia").exists()) {
            this.endPoint = "http://zapia.com.br:8880";
        } else {
            this.endPoint = "http://zapia.com.br:8080";
        }
        EventSource.Builder builder = new EventSource.Builder(this, URI.create("" + endPoint + "/api/eventos?token=" + token));
        EventSource eventSource = builder.build();
        eventSource.setReconnectionTimeMs(300);
        eventSource.start();
        this.source = eventSource;
    }

    public void setToken(String token) {
        if (!this.token.equals(token)) {
            if (source != null) {
                try {
                    source.close();
                } catch (Exception e) {

                }
            }
            EventSource.Builder builder = new EventSource.Builder(this, URI.create("" + endPoint + "/api/eventos?token=" + token));
            EventSource eventSource = builder.build();
            eventSource.setReconnectionTimeMs(300);
            eventSource.start();
            this.source = eventSource;
        }
    }

    public EventSource getSource() {
        return source;
    }

    public boolean notificarPedidoImpresso(Pedido pedido) {
        return Utilitarios.getResponseCode("" + endPoint + "/api/pedidoImpresso?uuid=" + pedido.getUuid() + "&token=" + token) == 201;
    }

    public boolean notificarReservaImpressa(Reserva reserva) {
        return Utilitarios.getResponseCode("" + endPoint + "/api/reservaImpressa?uuid=" + reserva.getUuid() + "&token=" + token) == 201;
    }

    public boolean cancelarPedido(Pedido pedido) {
        return Utilitarios.getResponseCode("" + endPoint + "/api/pedidoCancelado?uuid=" + pedido.getUuid() + "&token=" + token) == 201;
    }

    public boolean cancelarReserva(Reserva pedido) {
        return Utilitarios.getResponseCode("" + endPoint + "/api/excluirReserva?uuid=" + pedido.getUuid() + "&token=" + token) == 201;
    }

    public boolean concluirPedido(Pedido pedido, boolean aviso) {
        return Utilitarios.getResponseCode("" + endPoint + "/api/pedidoConcluido?uuid=" + pedido.getUuid() + "&notificar=" + aviso + "&token=" + token) == 201;
    }

    public boolean sairEntregaPedido(Pedido pedido, boolean aviso) {
        return Utilitarios.getResponseCode("" + endPoint + "/api/pedidoSaiuEntrega?uuid=" + pedido.getUuid() + "&notificar=" + aviso + "&token=" + token) == 201;
    }

    public List<Reserva> reservasAtivas() throws Throwable {
        String json = Utilitarios.getText("" + endPoint + "/api/reservas?token=" + token);
        if (!json.isEmpty()) {
            Reserva[] pedidos = builder.fromJson(json, Reserva[].class);
            return Arrays.asList(pedidos);
        } else {
            Throwable throwable = new Throwable("Falha ao baixar as reservas da api ");
            this.actionOnError.run(throwable);
            throw throwable;
        }
    }

    public List<Reserva> reservasImprimir() throws Throwable {
        String json = Utilitarios.getText("" + endPoint + "/api/reservasImprimir?token=" + token);
        if (!json.isEmpty()) {
            Reserva[] pedidos = builder.fromJson(json, Reserva[].class);
            return Arrays.asList(pedidos);
        } else {
            Throwable throwable = new Throwable("Falha ao baixar as reservas da api ");
            this.actionOnError.run(throwable);
            throw throwable;
        }
    }

    public Reserva reserva(UUID uuid) throws Throwable {
        String json = Utilitarios.getText("" + endPoint + "/api/reservas?token=" + token + "&uuid=" + uuid.toString());
        if (!json.isEmpty()) {
            Reserva reserva = builder.fromJson(json, Reserva.class);
            return reserva;
        } else {
            Throwable throwable = new Throwable("Falha ao baixar reserva da api ");
            this.actionOnError.run(throwable);
            throw throwable;
        }
    }

    public List<Pedido> pedidosImprimir() throws Throwable {
        String json = Utilitarios.getText("" + endPoint + "/api/pedidosImprimir?token=" + token);
        if (!json.isEmpty()) {
            Pedido[] pedidos = builder.fromJson(json, Pedido[].class);
            return Arrays.asList(pedidos);
        } else {
            Throwable throwable = new Throwable("Falha ao baixar os pedidos da api ");
            this.actionOnError.run(throwable);
            throw throwable;
        }
    }

    public Pedido pedido(UUID uuid) throws Throwable {
        String json = Utilitarios.getText("" + endPoint + "/api/pedido?token=" + token + "&uuid=" + uuid.toString());
        if (!json.isEmpty()) {
            Pedido pedido = builder.fromJson(json, Pedido.class);
            return pedido;
        } else {
            Throwable throwable = new Throwable("Falha ao baixar o pedido da api ");
            this.actionOnError.run(throwable);
            throw throwable;
        }
    }

    public List<Pedido> pedidosAtivos() throws Throwable {
        String json = Utilitarios.getText("" + endPoint + "/api/pedidosAtivos?token=" + token);
        if (!json.isEmpty()) {
            Pedido[] pedidos = builder.fromJson(json, Pedido[].class);
            return Arrays.asList(pedidos);
        } else {
            Throwable throwable = new Throwable("Falha ao baixar os pedidos da api ");
            this.actionOnError.run(throwable);
            throw throwable;
        }
    }

    @Override
    public void onOpen() throws Exception {
        if (this.open != null && !aleardyOpen) {
            aleardyOpen = true;
            open.run();
        }
    }

    @Override
    public void onClosed() throws Exception {
        if (this.disconnect != null) {
            disconnect.run();
        }
    }

    @Override
    public void onMessage(String s, MessageEvent messageEvent) throws Exception {
        if (s.equals("novo-pedido")) {
            if (actionOnNovoPedido != null) {
                String json = Utilitarios.getText("" + endPoint + "/api/pedido?uuid=" + messageEvent.getData() + "&token=" + this.token);
                if (!json.isEmpty()) {
                    Pedido pedido = builder.fromJson(json, Pedido.class);
                    actionOnNovoPedido.run(pedido);
                } else {
                    if (this.actionOnError != null) {
                        this.actionOnError.run(new Throwable("Falha ao baixar o pedido da api "));
                    }
                }
            }
        } else if (s.equals("nova-reserva")) {
            if (actionOnNovaReserva != null) {
                String json = Utilitarios.getText("" + endPoint + "/api/reservas?uuid=" + messageEvent.getData() + "&token=" + this.token);
                if (!json.isEmpty()) {
                    Reserva reserva = builder.fromJson(json, Reserva.class);
                    actionOnNovaReserva.run(reserva);
                } else {
                    if (this.actionOnError != null) {
                        this.actionOnError.run(new Throwable("Falha ao baixar reserva da api"));
                    }
                }
            }
        } else if (s.equals("logout")) {
            //this.source.close();
            if (onLogout != null) {
                onLogout.run();
            }
        } else if (s.equals("pedido-ajuda")) {
            if (actionOnAjuda != null) {
                actionOnAjuda.run(messageEvent.getData());
            }
        } else if (s.equals("atualizar-pedido")) {
            if (actionOnUpdatePedido != null) {
                String json = Utilitarios.getText("" + endPoint + "/api/pedido?uuid=" + messageEvent.getData() + "&token=" + this.token);
                if (!json.isEmpty()) {
                    Pedido pedido = builder.fromJson(json, Pedido.class);
                    actionOnUpdatePedido.run(pedido);
                } else {
                    if (this.actionOnError != null) {
                        this.actionOnError.run(new Throwable("Falha ao baixar o pedido da api "));
                    }
                }
            }
        }
    }

    @Override
    public void onComment(String s) throws Exception {

    }

    @Override
    public void onError(Throwable throwable) {
        if (this.actionOnError != null) {
            this.actionOnError.run(throwable);
        }
    }

    public interface ActionOnUpdatePedido {
        void run(Pedido pedido);
    }

    public interface ActionOnNovoPedido {
        void run(Pedido pedido);
    }

    public interface ActionOnNovaReserva {
        void run(Reserva reserva);
    }

    public interface ActionOnError {
        void run(Throwable throwable);
    }

    public interface ActionOnAjuda {
        void run(String nome);
    }
}

package visao;

import br.com.zapia.crack.JXBrowserCrack;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.DOMSelectElement;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleImpressao;
import controle.ControleLogin;
import evento.EventosImpressao;
import modelo.*;
import utils.ProtocolHandlerJar;
import utils.Utilitarios;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Inicio extends JFrame {
    private Browser browser;
    private BrowserView view;
    private Usuario usuario;
    private EventosImpressao eventosImpressao;
    private TrayImpressao trayImpressao;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private LocalDateTime lastPing;
    private String appdata;

    public Inicio() {
        try {
            appdata = System.getenv("APPDATA") + "\\Zapiá - Delivery\\";
            System.setProperty("jxbrowser.chromium.dir", appdata + "JxBrowser");
            init();
            this.setLocationRelativeTo(null);
            new JXBrowserCrack();
            browser = new Browser(new BrowserContext(
                    new BrowserContextParams(appdata + "/cache/" + this.getClass().getName())));
            view = new BrowserView(browser);
            ProtocolService protocolService = browser.getContext().getProtocolService();
            protocolService.setProtocolHandler("jar", new ProtocolHandlerJar());
            this.add(view);
        } catch (Exception ex) {
            fecharPrograma();
        }
    }

    private void fecharPrograma() {
        if (browser != null && !browser.isDisposed()) {
            browser.dispose();
        }
        System.exit(0);
    }

    private void init() {
        this.setTitle("Zapia Delivery System - Impressão");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.5)), ((int) (screenSize.getHeight() * 0.7))));
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                finalizar();
            }
        });
        pack();
    }

    public void deslogar() {
        if (eventosImpressao != null) {
            eventosImpressao.getSource().close();
        }
        executorService.shutdown();
        this.usuario = null;
        if (trayImpressao != null) {
            trayImpressao.remover();
            trayImpressao = null;
        }
    }

    public void updatePedido(Pedido p) {
        JSFunction function = browser.executeJavaScriptAndReturnValue("window.addPedido").asFunction();
        function.invoke(null, Utilitarios.getDefaultGsonBuilder(null).create().toJson(p), true);
    }

    public void addPedido(Pedido p) {
        JSFunction function = browser.executeJavaScriptAndReturnValue("window.addPedido").asFunction();
        function.invoke(null, Utilitarios.getDefaultGsonBuilder(null).create().toJson(p));
    }

    public void addReserva(Reserva r) {
        JSFunction function = browser.executeJavaScriptAndReturnValue("window.addReserva").asFunction();
        function.invoke(null, Utilitarios.getDefaultGsonBuilder(null).create().toJson(r));
    }

    public void imprimirPedido(Pedido pedido) {
        if (ControleImpressao.getInstance().imprimir(pedido)) {
            if (!eventosImpressao.notificarPedidoImpresso(pedido)) {
                JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
                function.invoke(null, "Ops!", "Ocorreu um erro ao marcar o pedido #" + pedido.getCod() + " como impresso, o suporte foi notificado!", "error");
            }
        } else {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao imprimir o pedido #" + pedido.getCod() + ".\r\nVerifique se a impressora está ligada corretamente.", "error");
        }
    }

    public void imprimirReserva(Reserva reserva) {
        /*if (ControleImpressao.getInstance().imprimir(reserva)) {
            if (!eventosImpressao.notificarReservaImpressa(reserva)) {
                JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
                function.invoke(null, "Ops!", "Ocorreu um erro ao marcar a reserva #" + reserva.getCod() + " como impressa, o suporte foi notificado!", "error");
            }
        } else {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao imprimir a reserva #" + reserva.getCod() + ".\r\nVerifique se a impressora está ligada corretamente.", "error");
        }*/
    }

    public void imprimirReserva(String uuid) {
        Reserva reserva = null;
        try {
            reserva = eventosImpressao.reserva(UUID.fromString(uuid));
        } catch (Throwable throwable) {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao buscar a reserva #" + reserva.getCod() + ", o suporte foi notificado!", "error");
            throwable.printStackTrace();
        }
        imprimirReserva(reserva);
    }

    public void imprimirPedido(String uuid) {
        Pedido pedido = null;
        try {
            pedido = eventosImpressao.pedido(UUID.fromString(uuid));
            imprimirPedido(pedido);
        } catch (Throwable throwable) {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao buscar o pedido #" + pedido.getCod() + ", o suporte foi notificado!", "error");
            throwable.printStackTrace();
        }
    }

    public void concluirPedido(String uuid, boolean aviso) {
        Pedido pedido = new Pedido();
        pedido.setUuid(UUID.fromString(uuid));
        if (!eventosImpressao.concluirPedido(pedido, aviso)) {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao marcar o pedido #" + pedido.getCod() + " como concluido, o suporte foi notificado!", "error");
        }
    }

    public void sairEntregaPedido(String uuid, boolean aviso) {
        Pedido pedido = new Pedido();
        pedido.setUuid(UUID.fromString(uuid));
        if (!eventosImpressao.sairEntregaPedido(pedido, aviso)) {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao marcar o pedido #" + pedido.getCod() + " como saindo para entrega, o suporte foi notificado!", "error");
        }
    }

    public void cancelarPedido(String uuid) {
        Pedido pedido = new Pedido();
        pedido.setUuid(UUID.fromString(uuid));
        if (!eventosImpressao.cancelarPedido(pedido)) {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao marcar o pedido #" + pedido.getCod() + " como cancelado, o suporte foi notificado!", "error");
        }
    }

    public void cancelarReserva(String uuid) {
        Reserva reserva = new Reserva();
        reserva.setUuid(UUID.fromString(uuid));
        if (!eventosImpressao.cancelarReserva(reserva)) {
            JSFunction function = browser.executeJavaScriptAndReturnValue("sAlert").asFunction();
            function.invoke(null, "Ops!", "Ocorreu um erro ao cancelar a reserva #" + reserva.getCod() + ", o suporte foi notificado!", "error");
        } else {
            atualizarLista();
        }
    }

    public void atualizarLista() {
        try {
            limparListas();
            for (Pedido pedido : eventosImpressao.pedidosAtivos()) {
                addPedido(pedido);
            }
            for (Reserva reserva : eventosImpressao.reservasAtivas()) {
                addReserva(reserva);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    public void trocarEstabelecimento() {
        eventosImpressao.getSource().close();
        trayImpressao.remover();
        trayImpressao = null;
    }

    public void realizarLogin(String login, String senha, final JSFunction jsFunction) {
        new Thread(() -> {
            try {
                Usuario usuario = ControleLogin.getInstance().getUsuario(login, senha);
                if (usuario != null) {
                    Inicio.this.usuario = usuario;
                    Inicio.this.usuario.setSenha(senha);
                    DOMSelectElement selectEstabelecimentos = (DOMSelectElement) browser.getDocument().findElement(By.id("estabelecimentos"));
                    for (DOMNode node : selectEstabelecimentos.getChildren()) {
                        selectEstabelecimentos.removeChild(node);
                    }
                    for (Estabelecimento estabelecimento : usuario.getEstabelecimentos()) {
                        DOMElement option = browser.getDocument().createElement("option");
                        option.setAttribute("value", estabelecimento.getUuid().toString());
                        option.setTextContent(estabelecimento.getNomeEstabelecimento());
                        selectEstabelecimentos.appendChild(option);
                    }
                    DOMSelectElement selectImpressoras = (DOMSelectElement) browser.getDocument().findElement(By.id("impressoras"));
                    for (DOMNode node : selectImpressoras.getChildren()) {
                        selectImpressoras.removeChild(node);
                    }
                    for (String impressora : ControleImpressao.getInstance().getImpressoras()) {
                        DOMElement option = browser.getDocument().createElement("option");
                        option.setAttribute("value", impressora);
                        option.setTextContent(impressora);
                        selectImpressoras.appendChild(option);
                    }
                    jsFunction.invoke(jsFunction.asObject(), true);
                } else {
                    jsFunction.invoke(jsFunction.asObject(), false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                jsFunction.invoke(jsFunction.asObject(), false);
            }
        }).start();
    }

    public void iniciarMonitoramento(String uuid, String impressora, JSFunction jsFunction) {
        new Thread(() -> {
            try {
                String token = ControleLogin.getInstance().getToken(usuario.getUsuario(), usuario.getSenha(), UUID.fromString(uuid));
                if (token.isEmpty()) {
                    jsFunction.invoke(jsFunction.asObject(), false);
                    return;
                }
                Configuracao.getInstance().setImpressaoHabilitada(true);
                Configuracao.getInstance().setNomeImpressora(impressora);
                for (Estabelecimento estabelecimento : usuario.getEstabelecimentos()) {
                    if (estabelecimento.getUuid().equals(UUID.fromString(uuid))) {
                        trayImpressao = new TrayImpressao(Inicio.this, estabelecimento.getNomeEstabelecimento());
                        if (!estabelecimento.getLogo().isEmpty()) {
                            byte[] btDataFile = Base64.getDecoder().decode(estabelecimento.getLogo().split(",")[1]);
                            BufferedImage image;
                            try {
                                image = ImageIO.read(new ByteArrayInputStream(btDataFile));
                                this.setIconImage(image.getScaledInstance(300, 300, Image.SCALE_DEFAULT));
                            } catch (IOException ex) {
                                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        Configuracao.getInstance().setNomeEstabelecimento(estabelecimento.getNomeEstabelecimento());
                        break;
                    }
                }
                //ControleImpressao.getInstance().imprimir(new File("logo.png"));
                Inicio.this.eventosImpressao = new EventosImpressao(token, (Pedido pedido) -> {
                    try {
                        trayImpressao.displayMenssage("Novo Pedido #" + pedido.getCod());
                        addPedido(pedido);
                        imprimirPedido(pedido);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, (Pedido pedido) -> {
                    try {
                        updatePedido(pedido);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, (Reserva reserva) -> {
                    trayImpressao.displayMenssage("Nova Reserva #" + reserva.getCod());
                    try {
                        addReserva(reserva);
                        imprimirReserva(reserva);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, this::imprimirPedido, (String nome) -> {
                    new Thread() {
                        public void run() {
                            trayImpressao.displayMenssage(nome + " está precisando de ajuda no WhatsApp");
                            JOptionPane.showMessageDialog(null, nome + " está precisando de ajuda no WhatsApp");
                        }
                    }.start();
                }, () -> {
                    limparListas();
                    try {
                        for (Pedido pedido : eventosImpressao.pedidosAtivos()) {
                            addPedido(pedido);
                            if (!pedido.isImpresso()) {
                                imprimirPedido(pedido);
                            }
                        }
                        for (Reserva reserva : eventosImpressao.reservasAtivas()) {
                            addReserva(reserva);
                            if (!reserva.isImpresso()) {
                                imprimirReserva(reserva);
                            }
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, null, null, () -> {
                    trayImpressao.displayMenssage("A conexão falhou, fazendo login novamente!");
                }, () -> {
                    lastPing = LocalDateTime.now();
                });
                executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.scheduleWithFixedDelay(() -> {
                    String tokeen = ControleLogin.getInstance().getToken(usuario.getUsuario(), usuario.getSenha(), UUID.fromString(uuid));
                    if (tokeen.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Falha ao renovar os dados de acesso, inicie o sistema novamente.");
                        fecharPrograma();
                    } else {
                        eventosImpressao.setToken(tokeen);
                    }
                }, 6, 6, TimeUnit.DAYS);
                executorService.scheduleWithFixedDelay(() -> {
                    if (lastPing.plusMinutes(2).isBefore(LocalDateTime.now())) {
                        System.out.println("Reconectado");
                        String tokeen = ControleLogin.getInstance().getToken(usuario.getUsuario(), usuario.getSenha(), UUID.fromString(uuid));
                        if (tokeen.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Falha ao renovar os dados de acesso, inicie o sistema novamente.");
                            fecharPrograma();
                        } else {
                            eventosImpressao.setToken(tokeen);
                        }
                    }
                }, 1, 1, TimeUnit.MINUTES);
                jsFunction.invoke(jsFunction.asObject(), true);
            } catch (Exception ex) {
                ex.printStackTrace();
                jsFunction.invoke(jsFunction.asObject(), false);
            }
        }).start();
    }

    private void limparListas() {
        DOMElement pedidos = browser.getDocument().findElement(By.id("pedidosList"));
        for (DOMNode node : pedidos.getChildren()) {
            pedidos.removeChild(node);
        }
        DOMElement reservas = browser.getDocument().findElement(By.id("reservasList"));
        for (DOMNode node : reservas.getChildren()) {
            reservas.removeChild(node);
        }
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/Login.html").toString());
            }
        });
        limparListas();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        this.setVisible(true);
    }

    public void finalizar() {
        if (trayImpressao == null) {
            int result = JOptionPane.showConfirmDialog(null, "Deseja realmente sair do sistema?\nObs: Nenhum pedido será impresso se o sistema estiver fechado!", "Atenção!!", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                fecharPrograma();
            }
        } else {
            trayImpressao.displayMenssage("Ainda em Execução");
            this.setVisible(false);
        }
    }

    public static void main(String[] args) {
        new Inicio().abrir();
    }


}

package modelo;


import visao.Inicio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TrayImpressao {

    String nomeMusica;
    TrayIcon trayIcon;
    Inicio telaImpressao;
    String nome;

    public void setTrayIcon() {
        this.trayIcon = null;
    }

    public void remover() {
        SystemTray tray = SystemTray.getSystemTray();
        tray.remove(trayIcon);
    }

    public TrayImpressao(Inicio tela, String nome) {
        this.nome = nome;
        this.telaImpressao = tela;
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            ImageIcon icone = new ImageIcon(getClass().getResource("/img/tray.png"));
            Image image = icone.getImage();
            ActionListener actionListener = e -> mostraEsconde();
            ActionListener sairListener = e -> {
                int result = JOptionPane.showConfirmDialog(null, "Deseja realmente sair do sistema?\nObs: Nenhum pedido será impresso se o sistema estiver fechado!", "Atenção!!", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    SystemTray.getSystemTray().remove(trayIcon);
                    System.exit(0);
                }
            };
            ActionListener MiniMaxi = e -> mostraEsconde();
            PopupMenu popup = new PopupMenu();
            MenuItem restauraItem = new MenuItem("Abrir");
            restauraItem.addActionListener(MiniMaxi);
            popup.add(restauraItem);
            popup.addSeparator();

            MenuItem sairItem = new MenuItem("Sair");
            sairItem.addActionListener(sairListener);
            popup.add(sairItem);
            popup.addSeparator();
            trayIcon = new TrayIcon(image, nome, popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon não pode ser adicionado no sistema.");
            }
        } else {
            System.err.println("Bandeja do sistema não é suportado.");
        }
    }

    public void mostraEsconde() {
        if (telaImpressao.isVisible()) {
            telaImpressao.setVisible(false);
        } else {
            telaImpressao.setVisible(true);
        }
    }

    public void displayMenssage(String nome) {
        this.nomeMusica = nome;
        trayIcon.displayMessage(nome, nome, TrayIcon.MessageType.INFO);
    }

    public void displayError(String nome) {
        this.nomeMusica = nome;
        trayIcon.displayMessage(nome, nome, TrayIcon.MessageType.ERROR);
    }
}

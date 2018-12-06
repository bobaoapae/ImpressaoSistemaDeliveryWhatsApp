package modelo;


import visao.Inicio;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

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
            ActionListener actionListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mostraEsconde();
                }

            };
            ActionListener sairListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int result = JOptionPane.showConfirmDialog(null, "Deseja realmente sair do sistema?\nObs: Nenhum pedido será impresso se o sistema estiver fechado!", "Atenção!!", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        SystemTray.getSystemTray().remove(trayIcon);
                        System.exit(0);
                    }
                }
            };
            ActionListener MiniMaxi = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mostraEsconde();
                }
            };
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

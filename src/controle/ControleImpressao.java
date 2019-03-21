package controle;

import email.com.gmail.ttsai0509.escpos.EscPosBuilder;
import email.com.gmail.ttsai0509.escpos.command.Align;
import email.com.gmail.ttsai0509.escpos.command.Cut;
import modelo.*;
import org.apache.commons.collections4.CollectionUtils;
import utils.Image;

import javax.imageio.ImageIO;
import javax.print.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ControleImpressao {

    private static ControleImpressao instance;

    public static ControleImpressao getInstance() {
        if (instance == null) {
            instance = new ControleImpressao();
        }
        return instance;
    }

    public enum TipoImpressao {
        IMPRESSAO_58_MM,
        IMPRESSAO_80MM
    }

    public boolean imprimir(File file) {
        try {
            EscPosBuilder builder = new EscPosBuilder().initialize();
            BufferedImage image = ImageIO.read(file);
            Image img = new Image();
            int[][] pixels = img.getPixelsSlow(image);
            byte[] LINE_SPACE_24 = {0x1b, 0x33, 24};
            byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33};
            byte[] CTL_LF = {0x0a};
            for (int y = 0; y < pixels.length; y += 24) {
                builder.raw(LINE_SPACE_24);
                builder.raw(SELECT_BIT_IMAGE_MODE);
                builder.raw(new byte[]{(byte) (0x00ff & pixels[y].length), (byte) ((0xff00 & pixels[y].length) >> 8)});
                for (int x = 0; x < pixels[y].length; x++) {
                    builder.raw(img.recollectSlice(y, x, pixels));
                }
                builder.raw(CTL_LF);
            }
            try {
                if (Configuracao.getInstance().isImpressaoHabilitada()) {
                    PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                    if (impressoraGeral == null) {
                        throw new Exception("Impressora Geral não instalada");
                    }
                    imprimirBytes(builder.getBytes(), impressoraGeral);
                } else {
                    //System.out.println(.toString());
                }
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean imprimir(Pedido p) {
        try {
            DecimalFormat moneyFormat = new DecimalFormat("###,###,###.00");
            EscPosBuilder builderImpressaoGeral = new EscPosBuilder().initialize();
            if (p.getNumeroMesa() <= 0) {
                builderImpressaoGeral.
                        font(email.com.gmail.ttsai0509.escpos.command.Font.DWDH_EMPHASIZED).align(Align.CENTER).text(Configuracao.getInstance().getNomeEstabelecimento() + "\r\n").font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR).feed(2);
                builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DWDH_EMPHASIZED).text("Ótima Noite\r\n" + p.getCliente().getNome() + "\r\n").text("Bom Apetite!").feed(6).cut(Cut.PART);
                builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
            }
            SimpleDateFormat formatadorData = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            builderImpressaoGeral.text(getStringWithSpacer("Data:", formatadorData.format(p.getDataPedido()), 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Pedido:", "#" + p.getCod() + "", 48, "."));
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
            if (p.getNumeroMesa() > 0) {
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text("Mesa: " + p.getNumeroMesa());
            }
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.align(Align.LEFT);
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DW);
            ArrayList<ItemPedido> aRemover = new ArrayList<>();
            synchronized (p.getProdutos()) {
                for (int x = 0; x < p.getProdutos().size(); x++) {
                    ItemPedido item = p.getProdutos().get(x);
                    Iterator<ItemPedido> iguais = p.getProdutos().stream().filter(o -> o.getProduto().equals(item.getProduto()) && item.getComentario().equals(((ItemPedido) o).getComentario()) && CollectionUtils.isEqualCollection(((ItemPedido) o).getAdicionais(), item.getAdicionais())).iterator();
                    if (aRemover.contains(item)) {
                        continue;
                    }
                    if (iguais.hasNext()) {
                        ItemPedido itemBase = iguais.next();
                        while (iguais.hasNext()) {
                            ItemPedido atual = iguais.next();
                            itemBase.setQtd(itemBase.getQtd() + atual.getQtd());
                            aRemover.add(atual);
                        }
                    }
                }
                p.getProdutos().removeAll(aRemover);
                for (ItemPedido item : p.getProdutos()) {
                    builderImpressaoGeral.text((getStringWithSpacer(item.getQtd() + " ", item.getProduto().getNomeWithCategories(), 22, ".")));
                    builderImpressaoGeral.text("\r\n");
                    Map<String, List<AdicionalProduto>> adicionaisGrupos = item.getAdicionaisGroupByGrupo();
                    for (Map.Entry<String, List<AdicionalProduto>> entry : adicionaisGrupos.entrySet()) {
                        builderImpressaoGeral.text("\n");
                        builderImpressaoGeral.text(entry.getKey() + ":" + "\r\n");
                        for (AdicionalProduto ad : entry.getValue()) {
                            builderImpressaoGeral.text(ad.getNome() + "\r\n");
                        }
                    }
                    if (item.getComentario() != null && !item.getComentario().isEmpty()) {
                        builderImpressaoGeral.text("\r\n");
                        builderImpressaoGeral.text((getStringWithSpacer("Obs:", item.getComentario(), 24, ".")));
                    }
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                    builderImpressaoGeral.text((getStringWithSpacer("Valor:", "R$ " + moneyFormat.format(item.getSubTotal()), 48, ".")));
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DW);
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 24, "-")));
                    builderImpressaoGeral.text("\r\n");
                }
            }
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.align(Align.LEFT);
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
            builderImpressaoGeral.text(getStringWithSpacer("Nome: ", p.getCliente().getNome(), 48, "."));
            builderImpressaoGeral.text("\r\n");
            if (!p.getCliente().getTelefoneMovel().isEmpty()) {
                builderImpressaoGeral.text(getStringWithSpacer("Celular: ", p.getCliente().getTelefoneMovel(), 48, "."));
                builderImpressaoGeral.text("\r\n");
            }
            if (!p.getCliente().getTelefoneFixo().isEmpty()) {
                builderImpressaoGeral.text(getStringWithSpacer("Fixo: ", p.getCliente().getTelefoneFixo(), 48, "."));
                builderImpressaoGeral.text("\r\n");
            }
            if (p.getNumeroMesa() == 0) {
                if (p.isEntrega()) {
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.EMPHASIZED);
                    builderImpressaoGeral.align(Align.CENTER);
                    builderImpressaoGeral.text("***Endereço para " + p.getTipoEntrega() == null ? "Entrega" : p.getTipoEntrega().getNome() + "***\r\n");
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                    builderImpressaoGeral.text(p.getEndereco().toString());
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                    builderImpressaoGeral.text("\r\n");
                } else {
                    builderImpressaoGeral.text(p.getTipoEntrega() == null ? "Retirar no Local" : p.getTipoEntrega().getNome());
                }
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text(getStringWithSpacer("Total: ", moneyFormat.format(p.getTotal()), 48, "."));
                builderImpressaoGeral.text("\r\n");
                if (p.isEntrega()) {
                    if (p.isCartao()) {
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.EMPHASIZED);
                        builderImpressaoGeral.align(Align.CENTER);
                        builderImpressaoGeral.text("***Levar Maquina de Cartao***\r\n");
                        if (p.getTroco() == -1) {
                            builderImpressaoGeral.text("Obs: " + p.getComentarioPedido() + "\r\n");
                        }
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                        builderImpressaoGeral.align(Align.LEFT);
                    } else if (p.getTroco() != 0) {
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.EMPHASIZED);
                        builderImpressaoGeral.text((getStringWithSpacer("Valor do Troco:", "R$" + moneyFormat.format(p.getTroco() - p.getTotal()), 48, ".")));
                        builderImpressaoGeral.text("\r\n");
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                        builderImpressaoGeral.align(Align.LEFT);
                    }
                    if (p.getHoraAgendamento() != null) {
                        builderImpressaoGeral.text((getStringWithSpacer("Horario para Entrega:", p.getHoraAgendamento().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), 48, ".")));
                    }
                } else {
                    if (p.getHoraAgendamento() != null) {
                        builderImpressaoGeral.text((getStringWithSpacer("Horario para Retirada:", p.getHoraAgendamento().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), 48, ".")));
                    }
                }
                builderImpressaoGeral.text("\r\n");
            }
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "#")));
            builderImpressaoGeral.feed(8).cut(Cut.FULL);
            byte[] textoGeral = removerAcentos(builderImpressaoGeral.toString()).getBytes();
            try {
                if (Configuracao.getInstance().isImpressaoHabilitada()) {
                    PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                    if (impressoraGeral == null) {
                        throw new Exception("Impressora Geral não instalada");
                    }
                    imprimirBytes(textoGeral, impressoraGeral);
                } else {
                    System.out.println(builderImpressaoGeral.toString());
                }
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public boolean imprimir(Reserva r) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            EscPosBuilder builderImpressaoGeral = new EscPosBuilder().initialize();
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
            builderImpressaoGeral.align(Align.CENTER);
            builderImpressaoGeral.text("SOLICITAÇÃO DE RESERVA");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.align(Align.LEFT);
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR).feed(2);
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Nome Contato: ", r.getNomeContato(), 42, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Telefone Contato: ", r.getTelefoneContato(), 42, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Número de Pessoas: ", r.getQtdPessoas() + "", 42, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Data Reserva: ", dateFormat.format(r.getDataReserva()), 42, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Horario Reserva: ", timeFormat.format(r.getDataReserva()), 42, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "#")));
            builderImpressaoGeral.feed(8).cut(Cut.FULL);
            byte[] textoGeral = removerAcentos(builderImpressaoGeral.toString()).getBytes();
            try {
                if (Configuracao.getInstance().isImpressaoHabilitada()) {
                    PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                    if (impressoraGeral == null) {
                        throw new Exception("Impressora Geral não instalada");
                    }
                    imprimirBytes(textoGeral, impressoraGeral);
                } else {
                    System.out.println(builderImpressaoGeral.toString());
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private PrintService selectImpress(String imp) {
        PrintService[] ps = PrintServiceLookup.lookupPrintServices(
                DocFlavor.INPUT_STREAM.AUTOSENSE, null);
        for (PrintService p : ps) {
            if (p.getName().equals(imp)) {
                return p;
            }
        }
        return null;
    }

    public List<String> getImpressoras() {
        List<String> impressoras = new ArrayList<>();
        PrintService[] ps = PrintServiceLookup.lookupPrintServices(
                DocFlavor.INPUT_STREAM.AUTOSENSE, null);
        for (PrintService p : ps) {
            impressoras.add(p.getName());
        }
        return impressoras;
    }

    private String getStringWithSpacer(String string1, String string2, int width, String spacer) {
        String resultado = removerAcentos(string1);
        while ((resultado + string2).length() < width) {
            resultado += spacer;
        }
        return removerAcentos(resultado + string2);
        //return string1+"..."+string2;
    }

    private static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    private synchronized void imprimirBytes(byte[] texto, PrintService impressora) throws Exception {
        DocPrintJob dpj = impressora.createPrintJob();
        SimpleDoc dimpDoc = new SimpleDoc(texto, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        dpj.print(dimpDoc, null);
    }
}

package controle;

import com.github.terrytsai.escpos.EscPosWriter;
import com.github.terrytsai.escpos.enums.*;
import modelo.AdicionalProduto;
import modelo.Configuracao;
import modelo.ItemPedido;
import modelo.Pedido;
import org.apache.commons.collections4.CollectionUtils;

import javax.print.*;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalTime;
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

    public boolean imprimir(Pedido p) {
        try {
            int horaAtual = LocalTime.now().getHour();
            String saudacao;
            if (horaAtual >= 2 && horaAtual < 12) {
                saudacao = "Ótimo Dia";
            } else if (horaAtual >= 12 && horaAtual < 18) {
                saudacao = "Ótima Tarde";
            } else {
                saudacao = "Ótima Noite";
            }
            DecimalFormat moneyFormat = new DecimalFormat("###,###,###.00");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            EscPosWriter builderImpressaoGeral = new EscPosWriter(byteArrayOutputStream)
                    .initialize()
                    .setCharacterCodeTable(CharacterCodeTable.PORTUGUESE).setJustification(Justification.CENTER);
            if (p.getNumeroMesa() <= 0) {
                builderImpressaoGeral.
                        setFont(Font.A).setCharacterSize(Width.X8, Height.X8).
                        setUnderline(Underline.ONE_DOT).
                        text(Configuracao.getInstance().getNomeEstabelecimento() + "\r\n").printAndFeedLines(2);
                builderImpressaoGeral.text(saudacao + "\r\n" + p.getCliente().getNome() + "\r\n").text("Bom Apetite!\r\n").cutWithFeed(CutB.PARTIAL, 6);
            }
            builderImpressaoGeral.setCharacterSize(Width.X1, Height.X1).setUnderline(Underline.CANCEL);
            builderImpressaoGeral.text(getStringWithSpacer("Data:", p.getDataPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Pedido:", "#" + p.getCod() + "", 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.setJustification(Justification.LEFT);
            builderImpressaoGeral.setFont(Font.A);
            ArrayList<ItemPedido> aRemover = new ArrayList<>();
            synchronized (p.getProdutos()) {
                for (int x = 0; x < p.getProdutos().size(); x++) {
                    ItemPedido item = p.getProdutos().get(x);
                    Iterator<ItemPedido> iguais = p.getProdutos().stream().filter(o -> o.getProduto().equals(item.getProduto()) && item.getComentario().equals(o.getComentario()) && CollectionUtils.isEqualCollection(o.getAdicionais(), item.getAdicionais())).iterator();
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
                    builderImpressaoGeral.text((getStringWithSpacer(item.getQtd() + " ", item.getProduto().getNomeWithCategories(), 48, ".")));
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
                        builderImpressaoGeral.text((getStringWithSpacer("Obs: ", item.getComentario(), 48, ".")));
                    }
                    builderImpressaoGeral.text((getStringWithSpacer("Valor: ", "R$ " + moneyFormat.format(item.getSubTotal()), 48, ".")));
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                    builderImpressaoGeral.text("\r\n");
                }
            }
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.setJustification(Justification.LEFT);
            builderImpressaoGeral.setFont(Font.A);
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
                    builderImpressaoGeral.setJustification(Justification.CENTER);
                    builderImpressaoGeral.text("***Endereço para " + (p.getTipoEntrega() == null ? "Entrega" : p.getTipoEntrega().getNome()) + "***\r\n");
                    builderImpressaoGeral.setFont(Font.A);
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                    builderImpressaoGeral.text(p.getEndereco().toString());
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                    builderImpressaoGeral.text("\r\n");
                } else {
                    builderImpressaoGeral.setJustification(Justification.CENTER);
                    builderImpressaoGeral.text("***" + (p.getTipoEntrega() == null ? "Retirar no Local" : p.getTipoEntrega().getNome()) + "***\r\n");
                    builderImpressaoGeral.text("\r\n");
                }
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "-")));
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text(getStringWithSpacer("Total: ", moneyFormat.format(p.getTotal()), 48, "."));
                builderImpressaoGeral.text("\r\n");
                if (p.isEntrega()) {
                    if (p.isCartao()) {
                        builderImpressaoGeral.setJustification(Justification.CENTER);
                        builderImpressaoGeral.text("***Levar Maquina de Cartao***\r\n");
                        if (p.getTroco() == -1) {
                            builderImpressaoGeral.text("Obs: " + p.getComentarioPedido() + "\r\n");
                        }
                        builderImpressaoGeral.setJustification(Justification.LEFT);
                    } else if (p.getTroco() != 0) {
                        builderImpressaoGeral.text((getStringWithSpacer("Valor do Troco: ", "R$" + moneyFormat.format(p.getTroco() - p.getTotal()), 48, ".")));
                        builderImpressaoGeral.text("\r\n");
                        builderImpressaoGeral.setJustification(Justification.LEFT);
                    }
                    if (p.getHoraAgendamento() != null) {
                        builderImpressaoGeral.text((getStringWithSpacer("Horario para Entrega: ", p.getHoraAgendamento().format(DateTimeFormatter.ofPattern("HH:mm")), 48, ".")));
                    }
                } else {
                    if (p.getHoraAgendamento() != null) {
                        builderImpressaoGeral.text((getStringWithSpacer("Horario para Retirada: ", p.getHoraAgendamento().format(DateTimeFormatter.ofPattern("HH:mm")), 48, ".")));
                    }
                }
                builderImpressaoGeral.text("\r\n");
            }
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text((getStringWithSpacer("", "", 48, "*")));
            builderImpressaoGeral.cutWithFeed(CutB.FULL, 8);
            byte[] textoGeral = removerAcentos(new String(byteArrayOutputStream.toByteArray())).getBytes();
            try {
                if (Configuracao.getInstance().isImpressaoHabilitada()) {
                    PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                    if (impressoraGeral == null) {
                        throw new Exception("Impressora " + Configuracao.getInstance().getNomeImpressora() + " não instalada");
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


    /*public boolean imprimir(Reserva r) {
        try {
            EscPosBuilder builderImpressaoGeral = new EscPosBuilder().initialize();
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
            builderImpressaoGeral.align(Align.CENTER);
            builderImpressaoGeral.text("SOLICITAÇÃO DE RESERVA");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.align(Align.LEFT);
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR).feed(2);
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Nome Contato: ", r.getNomeContato(), 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Telefone Contato: ", r.getTelefoneContato(), 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Número de Pessoas: ", r.getQtdPessoas() + "", 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Data Reserva: ", r.getDataReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 48, "."));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Horario Reserva: ", r.getDataReserva().format(DateTimeFormatter.ofPattern("HH:mm")), 48, "."));
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
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }*/

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

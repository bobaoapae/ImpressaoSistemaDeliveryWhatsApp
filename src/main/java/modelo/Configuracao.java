package modelo;

import controle.ControleImpressao;

public class Configuracao {
    private static Configuracao instance;
    private boolean impressaoHabilitada;
    private String nomeImpressora, nomeEstabelecimento;
    private ControleImpressao.TipoImpressao tipoImpressao;

    public static Configuracao getInstance() {
        if (instance == null) {
            instance = new Configuracao();
        }
        return instance;
    }

    public ControleImpressao.TipoImpressao getTipoImpressao() {
        return tipoImpressao;
    }

    public void setTipoImpressao(ControleImpressao.TipoImpressao tipoImpressao) {
        this.tipoImpressao = tipoImpressao;
    }

    public String getNomeEstabelecimento() {
        if (nomeEstabelecimento == null) {
            return "";
        }
        return nomeEstabelecimento;
    }

    public void setNomeEstabelecimento(String nomeEstabelecimento) {
        this.nomeEstabelecimento = nomeEstabelecimento;
    }

    public boolean isImpressaoHabilitada() {
        return impressaoHabilitada;
    }

    public void setImpressaoHabilitada(boolean impressaoHabilitada) {
        this.impressaoHabilitada = impressaoHabilitada;
    }

    public String getNomeImpressora() {
        if (nomeImpressora == null) {
            return "";
        }
        return nomeImpressora;
    }

    public void setNomeImpressora(String nomeImpressora) {
        this.nomeImpressora = nomeImpressora;
    }
}

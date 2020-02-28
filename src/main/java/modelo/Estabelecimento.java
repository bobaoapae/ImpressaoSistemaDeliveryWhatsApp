package modelo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Estabelecimento {

    private UUID uuid;
    private String nomeEstabelecimento, nomeBot, numeroAviso, webHookNovoPedido, webHookNovaReserva, logo;
    private int tempoMedioRetirada, tempoMedioEntrega;
    private boolean openPedidos, openChatBot, reservas, reservasComPedidosFechados, abrirFecharPedidosAutomaticamente;
    private boolean agendamentoDePedidos, ativo;
    private LocalDateTime horaAberturaPedidos;
    private LocalTime horaInicioReservas;
    private double taxaEntregaFixa, taxaEntregaKm, valorSelo;
    private List<Categoria> categorias;
    private List<Rodizio> rodizios;
    private int maximoSeloPorCompra, validadeSeloFidelidade;


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getNomeEstabelecimento() {
        return nomeEstabelecimento;
    }

    public void setNomeEstabelecimento(String nomeEstabelecimento) {
        this.nomeEstabelecimento = nomeEstabelecimento;
    }

    public String getNomeBot() {
        return nomeBot;
    }

    public void setNomeBot(String nomeBot) {
        this.nomeBot = nomeBot;
    }

    public String getNumeroAviso() {
        return numeroAviso;
    }

    public void setNumeroAviso(String numeroAviso) {
        this.numeroAviso = numeroAviso;
    }

    public String getWebHookNovoPedido() {
        return webHookNovoPedido;
    }

    public void setWebHookNovoPedido(String webHookNovoPedido) {
        this.webHookNovoPedido = webHookNovoPedido;
    }

    public String getWebHookNovaReserva() {
        return webHookNovaReserva;
    }

    public void setWebHookNovaReserva(String webHookNovaReserva) {
        this.webHookNovaReserva = webHookNovaReserva;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getTempoMedioRetirada() {
        return tempoMedioRetirada;
    }

    public void setTempoMedioRetirada(int tempoMedioRetirada) {
        this.tempoMedioRetirada = tempoMedioRetirada;
    }

    public int getTempoMedioEntrega() {
        return tempoMedioEntrega;
    }

    public void setTempoMedioEntrega(int tempoMedioEntrega) {
        this.tempoMedioEntrega = tempoMedioEntrega;
    }

    public boolean isOpenPedidos() {
        return openPedidos;
    }

    public void setOpenPedidos(boolean openPedidos) {
        this.openPedidos = openPedidos;
    }

    public boolean isOpenChatBot() {
        return openChatBot;
    }

    public void setOpenChatBot(boolean openChatBot) {
        this.openChatBot = openChatBot;
    }

    public boolean isReservas() {
        return reservas;
    }

    public void setReservas(boolean reservas) {
        this.reservas = reservas;
    }

    public boolean isReservasComPedidosFechados() {
        return reservasComPedidosFechados;
    }

    public void setReservasComPedidosFechados(boolean reservasComPedidosFechados) {
        this.reservasComPedidosFechados = reservasComPedidosFechados;
    }

    public boolean isAbrirFecharPedidosAutomaticamente() {
        return abrirFecharPedidosAutomaticamente;
    }

    public void setAbrirFecharPedidosAutomaticamente(boolean abrirFecharPedidosAutomaticamente) {
        this.abrirFecharPedidosAutomaticamente = abrirFecharPedidosAutomaticamente;
    }

    public boolean isAgendamentoDePedidos() {
        return agendamentoDePedidos;
    }

    public void setAgendamentoDePedidos(boolean agendamentoDePedidos) {
        this.agendamentoDePedidos = agendamentoDePedidos;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getHoraAberturaPedidos() {
        return horaAberturaPedidos;
    }

    public void setHoraAberturaPedidos(LocalDateTime horaAberturaPedidos) {
        this.horaAberturaPedidos = horaAberturaPedidos;
    }

    public LocalTime getHoraInicioReservas() {
        return horaInicioReservas;
    }

    public void setHoraInicioReservas(LocalTime horaInicioReservas) {
        this.horaInicioReservas = horaInicioReservas;
    }

    public double getTaxaEntregaFixa() {
        return taxaEntregaFixa;
    }

    public void setTaxaEntregaFixa(double taxaEntregaFixa) {
        this.taxaEntregaFixa = taxaEntregaFixa;
    }

    public double getTaxaEntregaKm() {
        return taxaEntregaKm;
    }

    public void setTaxaEntregaKm(double taxaEntregaKm) {
        this.taxaEntregaKm = taxaEntregaKm;
    }

    public double getValorSelo() {
        return valorSelo;
    }

    public void setValorSelo(double valorSelo) {
        this.valorSelo = valorSelo;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public List<Rodizio> getRodizios() {
        return rodizios;
    }

    public void setRodizios(List<Rodizio> rodizios) {
        this.rodizios = rodizios;
    }

    public int getMaximoSeloPorCompra() {
        return maximoSeloPorCompra;
    }

    public void setMaximoSeloPorCompra(int maximoSeloPorCompra) {
        this.maximoSeloPorCompra = maximoSeloPorCompra;
    }

    public int getValidadeSeloFidelidade() {
        return validadeSeloFidelidade;
    }

    public void setValidadeSeloFidelidade(int validadeSeloFidelidade) {
        this.validadeSeloFidelidade = validadeSeloFidelidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estabelecimento that = (Estabelecimento) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return this.getNomeEstabelecimento();
    }
}

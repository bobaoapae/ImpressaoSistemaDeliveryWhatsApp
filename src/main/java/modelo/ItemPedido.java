/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utils.Utilitarios;

import java.util.*;

/**
 * @author jvbor
 */
public class ItemPedido implements Comparable<ItemPedido> {

    private UUID uuid, uuid_pedido, uuid_produto;
    private Produto produto;
    private int qtd, qtdPago;
    private String comentario;
    private List<AdicionalProduto> adicionais;
    private boolean removido;
    private double subTotal, valorPago;
    private Pedido pedido;
    private JsonElement adicionaisPorGrupo;

    public ItemPedido() {
        adicionais = new ArrayList<>();
        comentario = "";
        qtd = 1;
    }

    public Map<String, List<AdicionalProduto>> getAdicionaisGroupByGrupo() {
        JsonArray array = adicionaisPorGrupo.getAsJsonArray();
        HashMap<String, List<AdicionalProduto>> listHashMap = new HashMap<>();
        for (int x = 0; x < array.size(); x++) {
            JsonObject object = array.get(x).getAsJsonObject();
            listHashMap.put(object.get("nomeGrupo").getAsString(), Arrays.asList(Utilitarios.getDefaultGsonBuilder(null).create().fromJson(object.get("adicionais"), AdicionalProduto[].class)));
        }
        return listHashMap;
    }

    public JsonElement getAdicionaisPorGrupo() {
        return adicionaisPorGrupo;
    }

    public void setAdicionaisPorGrupo(JsonElement adicionaisPorGrupo) {
        this.adicionaisPorGrupo = adicionaisPorGrupo;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public boolean isRemovido() {
        return removido;
    }

    public void setRemovido(boolean removido) {
        this.removido = removido;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getComentario() {
        if (comentario == null) {
            comentario = "";
        }
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public int getQtdPago() {
        return qtdPago;
    }

    public void setQtdPago(int qtdPago) {
        this.qtdPago = qtdPago;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid_pedido() {
        return uuid_pedido;
    }

    public void setUuid_pedido(UUID uuid_pedido) {
        this.uuid_pedido = uuid_pedido;
    }

    public UUID getUuid_produto() {
        return uuid_produto;
    }

    public void setUuid_produto(UUID uuid_produto) {
        this.uuid_produto = uuid_produto;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public double getValorPago() {
        return valorPago;
    }

    public List<AdicionalProduto> getAdicionais() {
        return adicionais;
    }

    public void setAdicionais(List<AdicionalProduto> adicionais) {
        this.adicionais = adicionais;
    }

    public List<AdicionalProduto> getAdicionais(GrupoAdicional grupoAdicional) {
        List<AdicionalProduto> temp = new ArrayList<>();
        Iterator<AdicionalProduto> it = getAdicionais().stream().filter((o) -> o.getGrupoAdicional().equals(grupoAdicional)).iterator();
        while (it.hasNext()) {
            temp.add(it.next());
        }
        return temp;
    }

    public boolean addAdicional(AdicionalProduto ad) {
        int contTotalGrupo = getAdicionais(ad.getGrupoAdicional()).size();
        if (contTotalGrupo < ad.getGrupoAdicional().getQtdMax()) {
            getAdicionais().add(ad);
            return true;
        }
        return false;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    @Override
    public int compareTo(ItemPedido t) {
        Integer otherCategory = t.getProduto().sequenceNr();
        Integer thisCategory = getProduto().sequenceNr();
        return thisCategory.compareTo(otherCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedido that = (ItemPedido) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

package modelo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Produto implements Comparable<Produto> {

    private UUID uuid, uuid_categoria;
    private transient Categoria categoria;
    private String nome, descricao, foto, nomeWithCategories;
    private double valor;
    private boolean onlyLocal, ativo, visivel;
    private RestricaoVisibilidade restricaoVisibilidade;
    private List<GrupoAdicional> gruposAdicionais;

    public String getFoto() {
        if (foto == null) {
            return "";
        }
        return foto;
    }

    public void setNomeWithCategories(String nomeWithCategories) {
        this.nomeWithCategories = nomeWithCategories;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public List<GrupoAdicional> getGruposAdicionais() {
        return gruposAdicionais;
    }

    public void setGruposAdicionais(List<GrupoAdicional> gruposAdicionais) {
        this.gruposAdicionais = Collections.synchronizedList(gruposAdicionais);
    }

    public RestricaoVisibilidade getRestricaoVisibilidade() {
        return restricaoVisibilidade;
    }

    public void setRestricaoVisibilidade(RestricaoVisibilidade restricaoVisibilidade) {
        this.restricaoVisibilidade = restricaoVisibilidade;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid_categoria() {
        return uuid_categoria;
    }

    public void setUuid_categoria(UUID uuid_categoria) {
        this.uuid_categoria = uuid_categoria;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        if (nome == null) {
            return "";
        }
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        if (descricao == null) {
            return "";
        }
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isOnlyLocal() {
        return onlyLocal;
    }

    public void setOnlyLocal(boolean onlyLocal) {
        this.onlyLocal = onlyLocal;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int sequenceNr() {
        if (this.getCategoria() != null) {
            return this.getCategoria().getOrdemExibicao();
        } else {
            return 999999999;
        }
    }

    @Override
    public int compareTo(Produto t) {
        return Integer.compare(this.sequenceNr(), t.sequenceNr());
    }

    public String getNomeWithCategories() {
        if (nomeWithCategories == null) {
            return "";
        }
        return nomeWithCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(uuid, produto.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

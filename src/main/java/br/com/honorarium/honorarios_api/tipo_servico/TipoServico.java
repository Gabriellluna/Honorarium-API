package br.com.honorarium.honorarios_api.tipo_servico;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_servico")
public class TipoServico {

    @Id
    private Short id;

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    public Short getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    @Column(name = "descricao", nullable = false, unique = true)
    private String descricao;
}

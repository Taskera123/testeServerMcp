package com.seplag.artistalbum.domain.banda.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "banda", uniqueConstraints = {
        @UniqueConstraint(name = "uk_banda_nomeBanda", columnNames = "nomeBanda")
})
public class BandaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBanda")
    private Long idBanda;

    @Column(name = "nomeBanda", nullable = false, length = 255)
    private String nomeBanda;

    @Column(name = "dataCriacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "dataAtualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    /* =========================
       Relacionamento N:N (via entidade de junção)
       ========================= */
    @OneToMany(
            mappedBy = "banda",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<BandaArtistaModel> artistas = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Long getIdBanda() {
        return idBanda;
    }

    public void setIdBanda(Long idBanda) {
        this.idBanda = idBanda;
    }

    public String getNomeBanda() {
        return nomeBanda;
    }

    public void setNomeBanda(String nomeBanda) {
        this.nomeBanda = nomeBanda;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public Set<BandaArtistaModel> getArtistas() {
        return artistas;
    }

    public void setArtistas(Set<BandaArtistaModel> artistas) {
        this.artistas = artistas;
    }
}

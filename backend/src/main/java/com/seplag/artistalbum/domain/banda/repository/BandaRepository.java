package com.seplag.artistalbum.domain.banda.repository;

import com.seplag.artistalbum.domain.banda.model.BandaModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BandaRepository extends JpaRepository<BandaModel, Long> {

    Optional<BandaModel> findByNomeBandaIgnoreCase(String nomeBanda);

    boolean existsByNomeBandaIgnoreCase(String nomeBanda);

    // Carrega a tabela de junção (bandaArtista)
    @EntityGraph(attributePaths = {"artistas"})
    Optional<BandaModel> findWithArtistasByIdBanda(Long idBanda);

    @EntityGraph(attributePaths = {"artistas"})
    List<BandaModel> findAll();

    @Query("""
        select b from BandaModel b
        left join fetch b.artistas ba
        left join fetch ba.artista a
        where b.idBanda = :idBanda
    """)
    Optional<BandaModel> findByIdWithArtistasDetalhados(@Param("idBanda") Long idBanda);


}

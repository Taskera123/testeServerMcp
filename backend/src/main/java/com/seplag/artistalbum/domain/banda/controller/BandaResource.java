package com.seplag.artistalbum.domain.banda.controller;

import com.seplag.artistalbum.domain.artista.dto.ArtistaResumoDTO;
import com.seplag.artistalbum.domain.banda.dto.BandaRequestDTO;
import com.seplag.artistalbum.domain.banda.dto.VincularArtistaRequestDTO;
import com.seplag.artistalbum.domain.banda.dto.BandaResponseDTO;
import com.seplag.artistalbum.domain.banda.service.BandaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/bandas")
@Tag(name = "Bandas", description = "APIs de gerenciamento de bandas")
public class BandaResource {

    @Autowired
    private final BandaService bandaService;

    public BandaResource(BandaService bandaService) {
        this.bandaService = bandaService;
    }

    /* CREATE */
    @PostMapping
    public ResponseEntity<BandaResponseDTO> criar(@Valid @RequestBody BandaRequestDTO request) {
        BandaResponseDTO criado = bandaService.criarBanda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /* READ by id */
    @GetMapping("/{idBanda}")
    public ResponseEntity<BandaResponseDTO> obterPorId(@PathVariable Long idBanda) {
        return ResponseEntity.ok(bandaService.obterBandaPorId(idBanda));
    }

    /* READ list */
    @GetMapping
    public ResponseEntity<List<BandaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(bandaService.listarTodas());
    }

    /* READ paged */
    @GetMapping("/paginado")
    public ResponseEntity<Page<BandaResponseDTO>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(bandaService.listarPaginado(page, size, sortDir));
    }

    /* UPDATE */
    @PutMapping("/{idBanda}")
    public ResponseEntity<BandaResponseDTO> atualizar(
            @PathVariable Long idBanda,
            @Valid @RequestBody BandaRequestDTO request
    ) {
        return ResponseEntity.ok(bandaService.atualizarBanda(idBanda, request));
    }

    /* DELETE */
    @DeleteMapping("/{idBanda}")
    public ResponseEntity<Void> deletar(@PathVariable Long idBanda) {
        bandaService.deletarBanda(idBanda);
        return ResponseEntity.noContent().build();
    }

    /* VINCULAR ARTISTA NA BANDA */
    @PostMapping("/{idBanda}/artistas")
    public ResponseEntity<Void> vincularArtista(
            @PathVariable Long idBanda,
            @Valid @RequestBody VincularArtistaRequestDTO request
    ) {
        bandaService.vincularArtista(idBanda, request.getIdArtista());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /* DESVINCULAR ARTISTA DA BANDA */
    @DeleteMapping("/{idBanda}/artistas/{idArtista}")
    public ResponseEntity<Void> desvincularArtista(
            @PathVariable Long idBanda,
            @PathVariable Long idArtista
    ) {
        bandaService.desvincularArtista(idBanda, idArtista);
        return ResponseEntity.noContent().build();
    }

    /* LISTAR ARTISTAS DA BANDA */
    @GetMapping("/{idBanda}/artistas")
    public ResponseEntity<List<ArtistaResumoDTO>> listarArtistasDaBanda(@PathVariable Long idBanda) {
        return ResponseEntity.ok(bandaService.listarArtistasDaBanda(idBanda));
    }
}

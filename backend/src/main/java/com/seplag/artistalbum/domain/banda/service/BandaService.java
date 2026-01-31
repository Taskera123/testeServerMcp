package com.seplag.artistalbum.domain.banda.service;

import com.seplag.artistalbum.domain.artista.dto.ArtistaResumoDTO;
import com.seplag.artistalbum.domain.banda.mapper.BandaMapper;
import com.seplag.artistalbum.domain.banda.dto.BandaRequestDTO;
import com.seplag.artistalbum.domain.banda.dto.BandaResponseDTO;
import com.seplag.artistalbum.shared.exception.ResourceNotFoundException;
import com.seplag.artistalbum.domain.artista.model.ArtistaModel;
import com.seplag.artistalbum.domain.banda.model.BandaArtistaId;
import com.seplag.artistalbum.domain.banda.model.BandaArtistaModel;
import com.seplag.artistalbum.domain.banda.model.BandaModel;
import com.seplag.artistalbum.domain.artista.repository.ArtistaRepository;
import com.seplag.artistalbum.domain.banda.repository.BandaArtistaRepository;
import com.seplag.artistalbum.domain.banda.repository.BandaRepository;
import com.seplag.artistalbum.shared.websocket.UpdateMessage;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BandaService {

    private final BandaRepository bandaRepository;
    private final ArtistaRepository artistaRepository;
    private final BandaArtistaRepository bandaArtistaRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BandaService(
            BandaRepository bandaRepository,
            ArtistaRepository artistaRepository,
            BandaArtistaRepository bandaArtistaRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.bandaRepository = bandaRepository;
        this.artistaRepository = artistaRepository;
        this.bandaArtistaRepository = bandaArtistaRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /* =========================
       CREATE
       ========================= */
    @Transactional
    public BandaResponseDTO criarBanda(BandaRequestDTO request) {
        if (bandaRepository.existsByNomeBandaIgnoreCase(request.getNomeBanda())) {
            throw new DataIntegrityViolationException("Já existe uma banda com este nome.");
        }

        BandaModel banda = new BandaModel();
        banda.setNomeBanda(request.getNomeBanda());

        BandaModel salva = bandaRepository.save(banda);
        enviarAtualizacao("created", salva.getIdBanda());
        return BandaMapper.toResponseDTO(salva);
    }

    /* =========================
       READ por id (com artistas)
       ========================= */
    @Transactional
    public BandaResponseDTO obterBandaPorId(Long idBanda) {
        BandaModel banda = bandaRepository.findByIdWithArtistasDetalhados(idBanda)
                .orElseThrow(() -> new ResourceNotFoundException("Banda não encontrada. idBanda=" + idBanda));

        return BandaMapper.toResponseDTO(banda);
    }

    /* =========================
       READ listar todas (sem pagina)
       ========================= */
    @Transactional
    public List<BandaResponseDTO> listarTodas() {
        return bandaRepository.findAll(Sort.by("nomeBanda").ascending())
                .stream()
                .map(BandaMapper::toResponseDTO)
                .toList();
    }

    /* =========================
       READ listar paginado
       ========================= */
    @Transactional
    public Page<BandaResponseDTO> listarPaginado(int page, int size, String sortDir) {
        Sort sort = Sort.by("nomeBanda");
        sort = "desc".equalsIgnoreCase(sortDir) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bandaRepository.findAll(pageable).map(BandaMapper::toResponseDTO);
    }

    /* =========================
       UPDATE
       ========================= */
    @Transactional
    public BandaResponseDTO atualizarBanda(Long idBanda, BandaRequestDTO request) {
        BandaModel banda = bandaRepository.findById(idBanda)
                .orElseThrow(() -> new ResourceNotFoundException("Banda não encontrada. idBanda=" + idBanda));

        String novoNome = request.getNomeBanda();
        boolean nomeMudou = novoNome != null && !novoNome.equalsIgnoreCase(banda.getNomeBanda());

        if (nomeMudou && bandaRepository.existsByNomeBandaIgnoreCase(novoNome)) {
            throw new DataIntegrityViolationException("Já existe uma banda com este nome.");
        }

        banda.setNomeBanda(novoNome);
        BandaModel salva = bandaRepository.save(banda);
        enviarAtualizacao("updated", salva.getIdBanda());

        return BandaMapper.toResponseDTO(salva);
    }

    /* =========================
       DELETE
       ========================= */
    @Transactional
    public void deletarBanda(Long idBanda) {
        if (!bandaRepository.existsById(idBanda)) {
            throw new ResourceNotFoundException("Banda não encontrada. idBanda=" + idBanda);
        }
        bandaRepository.deleteById(idBanda);
        enviarAtualizacao("deleted", idBanda);
    }

    /* =========================
       VINCULAR ARTISTA NA BANDA
       ========================= */
    @Transactional
    public void vincularArtista(Long idBanda, Long idArtista) {
        BandaModel banda = bandaRepository.findById(idBanda)
                .orElseThrow(() -> new ResourceNotFoundException("Banda não encontrada. idBanda=" + idBanda));

        ArtistaModel artista = artistaRepository.findById(idArtista)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado. idArtista=" + idArtista));

        if (bandaArtistaRepository.existsByBanda_IdBandaAndArtista_IdArtista(idBanda, idArtista)) {
            // já existe vínculo, não faz nada (ou lance erro se preferir)
            return;
        }

        BandaArtistaModel ba = new BandaArtistaModel();
        ba.setId(new BandaArtistaId(idBanda, idArtista));
        ba.setBanda(banda);
        ba.setArtista(artista);

        bandaArtistaRepository.save(ba);
        enviarAtualizacao("linked", idBanda);
    }

    /* =========================
       DESVINCULAR ARTISTA DA BANDA
       ========================= */
    @Transactional
    public void desvincularArtista(Long idBanda, Long idArtista) {
        if (!bandaRepository.existsById(idBanda)) {
            throw new ResourceNotFoundException("Banda não encontrada. idBanda=" + idBanda);
        }
        if (!artistaRepository.existsById(idArtista)) {
            throw new ResourceNotFoundException("Artista não encontrado. idArtista=" + idArtista);
        }

        bandaArtistaRepository.deleteByBanda_IdBandaAndArtista_IdArtista(idBanda, idArtista);
        enviarAtualizacao("unlinked", idBanda);
    }

    /* =========================
       LISTAR ARTISTAS DA BANDA
       ========================= */
    @Transactional
    public List<ArtistaResumoDTO> listarArtistasDaBanda(Long idBanda) {
        BandaModel banda = bandaRepository.findByIdWithArtistasDetalhados(idBanda)
                .orElseThrow(() -> new ResourceNotFoundException("Banda não encontrada. idBanda=" + idBanda));

        return banda.getArtistas()
                .stream()
                .filter(ba -> ba.getArtista() != null)
                .map(ba -> new ArtistaResumoDTO(ba.getArtista().getIdArtista(), ba.getArtista().getNomeArtista()))
                .toList();
    }

    private void enviarAtualizacao(String acao, Long idBanda) {
        messagingTemplate.convertAndSend("/topic/updates", new UpdateMessage("banda", acao, idBanda));
    }
}

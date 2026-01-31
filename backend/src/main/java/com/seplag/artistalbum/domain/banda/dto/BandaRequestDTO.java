package com.seplag.artistalbum.domain.banda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BandaRequestDTO {

    @NotBlank
    @Size(max = 255)
    private String nomeBanda;

    public BandaRequestDTO() {
    }

    public String getNomeBanda() {
        return nomeBanda;
    }

    public void setNomeBanda(String nomeBanda) {
        this.nomeBanda = nomeBanda;
    }
}

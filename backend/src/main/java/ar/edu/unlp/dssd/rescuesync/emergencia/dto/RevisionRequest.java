package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import jakarta.validation.constraints.Size;

public record RevisionRequest(
        @Size(max = 4000, message = "Las observaciones admiten hasta 4000 caracteres") String observaciones) {
}

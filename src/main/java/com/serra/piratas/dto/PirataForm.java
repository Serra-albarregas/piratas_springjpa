package com.serra.piratas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO de FORMULARIO: solo tiene los campos que el formulario puede cambiar.
//  - No tiene id: nadie puede sobrescribir un pirata existente enviando un id.
//  - La tripulación y la fruta llegan como IDENTIFICADORES (tripulacionId, frutaId):
//    es el servicio quien los convierte en entidades. Ya no hacen falta Formatter.
//  - Las validaciones del formulario viven aquí, separadas de las anotaciones de JPA.
@Data
public class PirataForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;

    @NotNull(message = "Debes elegir una tripulación")
    private Long tripulacionId;

    private Long frutaId; // opcional

    @NotNull(message = "La recompensa es obligatoria")
    @PositiveOrZero(message = "La recompensa no puede ser negativa")
    private Long recompensa;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;
}

package com.serra.piratas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// El servicio no debe depender de clases web como ResponseStatusException.
// Con @ResponseStatus, Spring convierte esta excepción en una respuesta 404.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

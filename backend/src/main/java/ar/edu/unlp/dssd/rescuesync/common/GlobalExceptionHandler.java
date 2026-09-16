package ar.edu.unlp.dssd.rescuesync.common;

import ar.edu.unlp.dssd.rescuesync.bpm.BpmException;
import ar.edu.unlp.dssd.rescuesync.bpm.BpmSesionExpiradaException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones a respuestas RFC 7807 sin exponer stacktraces a la SPA. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoEncontradoException.class)
    ProblemDetail noEncontrado(NoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail reglaNegocio(ReglaNegocioException e) {
        return problema(HttpStatus.CONFLICT, "Operación no permitida", e.getMessage());
    }

    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    ProblemDetail accesoDenegado(RuntimeException e) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", "No tiene permisos para realizar esta operación");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        e.getBindingResult().getGlobalErrors()
                .forEach(error -> errores.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Datos inválidos", "Revise los campos marcados");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(CampoInvalidoException.class)
    ProblemDetail campoInvalido(CampoInvalidoException e) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Datos inválidos", e.getMessage());
        problema.setProperty("errores", Map.of(e.getCampo(), e.getMessage()));
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail cuerpoIlegible(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", "El cuerpo de la solicitud no es válido");
    }

    @ExceptionHandler(BpmSesionExpiradaException.class)
    ProblemDetail sesionBpmExpirada(BpmSesionExpiradaException e) {
        ProblemDetail problema = problema(HttpStatus.UNAUTHORIZED, "Sesión expirada",
                "La sesión con el motor de procesos expiró. Vuelva a iniciar sesión.");
        problema.setProperty("codigo", "BPM_SESION_EXPIRADA");
        return problema;
    }

    @ExceptionHandler(BpmException.class)
    ProblemDetail errorBpm(BpmException e) {
        log.error("Error en la integración con el BPM", e);
        return problema(HttpStatus.BAD_GATEWAY, "Error del motor de procesos", e.getMessage());
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}

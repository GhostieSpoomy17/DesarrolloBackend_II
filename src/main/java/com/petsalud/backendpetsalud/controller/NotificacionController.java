package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Notificacion;
import com.petsalud.backendpetsalud.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @PostMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Notificacion> enviarNotificacion(@PathVariable Long usuarioId, @RequestBody Notificacion notificacion) {
        Notificacion nuevaNotificacion = notificacionService.enviarNotificacion(usuarioId, notificacion);
        return ResponseEntity.ok(nuevaNotificacion);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE') or (hasAuthority('CLIENTE') and @permisos.esNotificacionPropia(authentication, #id))")
    public ResponseEntity<Notificacion> obtenerNotificacionPorId(@PathVariable Long id) {
        return notificacionService.obtenerNotificacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE') or (hasAuthority('CLIENTE') and @permisos.esUsuario(authentication, #usuarioId))")
    public ResponseEntity<List<Notificacion>> listarNotificacionesPorUsuario(@PathVariable Long usuarioId) {
        List<Notificacion> notificaciones = notificacionService.listarNotificacionesPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE') or (hasAuthority('CLIENTE') and @permisos.esUsuario(authentication, #usuarioId))")
    public ResponseEntity<List<Notificacion>> listarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        List<Notificacion> notificaciones = notificacionService.listarNotificacionesNoLeidas(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @PutMapping("/{id}/leida")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE') or (hasAuthority('CLIENTE') and @permisos.esNotificacionPropia(authentication, #id))")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GERENTE')")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}

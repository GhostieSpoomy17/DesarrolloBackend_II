package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Recompensa;
import com.petsalud.backendpetsalud.service.RecompensaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recompensas")
public class RecompensaController {

    @Autowired
    private RecompensaService recompensaService;

    @PostMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Recompensa> agregarRecompensa(@PathVariable Long usuarioId, @RequestBody Recompensa recompensa) {
        Recompensa nuevaRecompensa = recompensaService.agregarRecompensa(usuarioId, recompensa);
        return ResponseEntity.ok(nuevaRecompensa);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE') or (hasAuthority('CLIENTE') and @permisos.esRecompensaPropia(authentication, #id))")
    public ResponseEntity<Recompensa> obtenerRecompensaPorId(@PathVariable Long id) {
        return recompensaService.obtenerRecompensaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'GERENTE') or (hasAuthority('CLIENTE') and @permisos.esUsuario(authentication, #usuarioId))")
    public ResponseEntity<List<Recompensa>> listarRecompensasPorUsuario(@PathVariable Long usuarioId) {
        List<Recompensa> recompensas = recompensaService.listarRecompensasPorUsuario(usuarioId);
        return ResponseEntity.ok(recompensas);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GERENTE')")
    public ResponseEntity<Void> eliminarRecompensa(@PathVariable Long id) {
        recompensaService.eliminarRecompensa(id);
        return ResponseEntity.noContent().build();
    }
}

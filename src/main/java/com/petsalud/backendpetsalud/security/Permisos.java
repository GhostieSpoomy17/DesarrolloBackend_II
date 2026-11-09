package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.ExpedienteMedicoRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.RecompensaRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permisos")
public class Permisos {
    private final MascotaRepository mascotas;
    private final CitaRepository citas;
    private final ExpedienteMedicoRepository expedientes;
    private final NotificacionRepository notificaciones;
    private final RecompensaRepository recompensas;
    private final VentaRepository ventas;

    public Permisos(MascotaRepository mascotas, CitaRepository citas, ExpedienteMedicoRepository expedientes,
                    NotificacionRepository notificaciones, RecompensaRepository recompensas, VentaRepository ventas) {
        this.mascotas = mascotas;
        this.citas = citas;
        this.expedientes = expedientes;
        this.notificaciones = notificaciones;
        this.recompensas = recompensas;
        this.ventas = ventas;
    }

    public boolean esUsuario(Authentication auth, Long id) {
        return id(auth).equals(id);
    }

    public boolean esMascotaPropia(Authentication auth, Long mascotaId) {
        return mascotas.existsByIdAndPropietarioId(mascotaId, id(auth));
    }

    public boolean esCitaPropia(Authentication auth, Long citaId) {
        return citas.existsByIdAndMascotaPropietarioId(citaId, id(auth));
    }

    public boolean esExpedientePropio(Authentication auth, Long expedienteId) {
        return expedientes.existsByIdAndMascotaPropietarioId(expedienteId, id(auth));
    }

    public boolean esNotificacionPropia(Authentication auth, Long notificacionId) {
        return notificaciones.existsByIdAndUsuarioId(notificacionId, id(auth));
    }

    public boolean esRecompensaPropia(Authentication auth, Long recompensaId) {
        return recompensas.existsByIdAndUsuarioId(recompensaId, id(auth));
    }

    public boolean esVentaPropia(Authentication auth, Long ventaId) {
        return ventas.existsByIdAndUsuarioId(ventaId, id(auth));
    }

    private Long id(Authentication auth) {
        return ((UsuarioDetails) auth.getPrincipal()).getId();
    }
}

package com.petsalud.backendpetsalud.entity;

public enum Rol {
    CLIENTE,
    VETERINARIO,
    ADMIN,
    EMPLEADO,
    GERENTE;

    public String permiso() {
        return switch (this) {
            case VETERINARIO -> EMPLEADO.name();
            case ADMIN -> GERENTE.name();
            default -> name();
        };
    }
}

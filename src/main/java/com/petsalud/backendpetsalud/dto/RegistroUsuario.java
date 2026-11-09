package com.petsalud.backendpetsalud.dto;

public record RegistroUsuario(String email, String password, String nombre, String apellido,
                              String telefono, String direccion) {}

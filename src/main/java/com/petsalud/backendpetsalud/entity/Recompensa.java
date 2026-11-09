package com.petsalud.backendpetsalud.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Integer puntos;

    private String descripcion;

    private LocalDateTime fecha;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"email", "telefono", "direccion"})
    private Usuario usuario;
}

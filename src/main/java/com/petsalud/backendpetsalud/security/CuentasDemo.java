package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.dto.RegistroUsuario;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class CuentasDemo implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final UsuarioServiceImpl registro;
    private final String password;

    public CuentasDemo(UsuarioRepository usuarios, UsuarioServiceImpl registro,
                       @Value("${PETSALUD_DEMO_PASSWORD}") String password) {
        this.usuarios = usuarios;
        this.registro = registro;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        crear("empleado@petsalud.test", "Empleado", Rol.EMPLEADO);
        crear("gerente@petsalud.test", "Gerente", Rol.GERENTE);
    }

    private void crear(String email, String nombre, Rol rol) {
        if (usuarios.findByEmailIgnoreCase(email).isEmpty()) {
            registro.crearCuenta(new RegistroUsuario(email, password, nombre, null, null, null), rol);
        }
    }
}

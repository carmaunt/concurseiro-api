package br.com.concurseiro.api.usuarios.repository;

import br.com.concurseiro.api.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByFirebaseUid(String firebaseUid);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}

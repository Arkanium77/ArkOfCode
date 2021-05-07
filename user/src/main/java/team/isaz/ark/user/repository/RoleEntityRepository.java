package team.isaz.ark.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.isaz.ark.user.entity.RoleEntity;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, String> {
}

package team.isaz.ark.user.repository;

import org.springframework.data.repository.CrudRepository;
import team.isaz.ark.user.entity.RoleEntity;

public interface RoleEntityRepository extends CrudRepository<RoleEntity, String> {
}

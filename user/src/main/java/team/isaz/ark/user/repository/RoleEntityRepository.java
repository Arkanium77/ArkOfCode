package team.isaz.ark.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import team.isaz.ark.user.entity.RoleEntity;

import javax.management.relation.Role;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, String> {
}

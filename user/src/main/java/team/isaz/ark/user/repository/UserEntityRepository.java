package team.isaz.ark.user.repository;

import org.springframework.data.repository.CrudRepository;
import team.isaz.ark.user.entity.UserEntity;

import java.util.Optional;

public interface UserEntityRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByLogin(String login);
}

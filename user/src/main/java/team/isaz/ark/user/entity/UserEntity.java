package team.isaz.ark.user.entity;

import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_dttm")
    private OffsetDateTime createDttm;

    @Column(name = "modify_dttm")
    private OffsetDateTime modifyDttm;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "banned")
    private boolean isUserBanned;

    @Column(name = "token_verify_code")
    private UUID tokenVerifyCode;

    @ManyToOne
    @JoinColumn(name = "role")
    private RoleEntity role;

    @PrePersist
    private void persist() {
        if (createDttm == null) {
            createDttm = OffsetDateTime.now();
        }
        if (modifyDttm == null) {
            modifyDttm = OffsetDateTime.now();
        }
        if (tokenVerifyCode == null) {
            tokenVerifyCode = UUID.randomUUID();
        }
        isUserBanned = false;
    }

    @PreUpdate
    private void update() {
        if (modifyDttm == null) {
            modifyDttm = OffsetDateTime.now();
        }
        tokenVerifyCode = UUID.randomUUID();
    }
}

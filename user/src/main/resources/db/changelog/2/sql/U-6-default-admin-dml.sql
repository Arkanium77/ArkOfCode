INSERT INTO main.user (login, password, token_verify_code, role)
VALUES ('root', '$2a$10$Cl2MFiZGjulUAOljaBQA6uF9GjpnrQwQFuBmv5HOyPICZYhlDm7nq', '00000000-0000-0000-0000-000000000000', 'ROLE_ADMIN')
ON CONFLICT (login) DO UPDATE SET role = 'ROLE_ADMIN';
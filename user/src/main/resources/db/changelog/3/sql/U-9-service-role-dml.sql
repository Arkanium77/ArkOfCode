INSERT INTO main.role (name)
VALUES ('ROLE_SERVICE');

INSERT INTO main.user (login, password, token_verify_code, role)
VALUES ('_tech', 'null', '00000000-0000-0000-0000-000000000000', 'ROLE_SERVICE')
ON CONFLICT (login) DO UPDATE SET role = 'ROLE_SERVICE';
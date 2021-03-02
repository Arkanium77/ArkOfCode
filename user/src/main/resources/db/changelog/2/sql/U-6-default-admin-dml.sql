INSERT INTO main.user (login, password, token_verify_code, role)
VALUES ('root', '$2a$10$gch1/Gvi4Z/w7oBUgmI4C.m5yl6e8W6uxV3DnqQ7Sq0CzlsVSvUwe', '00000000-0000-0000-0000-000000000000', 'ROLE_ADMIN')
ON CONFLICT (login) DO UPDATE SET role = 'ROLE_ADMIN';
-- password = password
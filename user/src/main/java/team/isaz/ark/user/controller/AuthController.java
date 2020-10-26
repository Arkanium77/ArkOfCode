package team.isaz.ark.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.configuration.jwt.JwtProvider;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.UserService;

import javax.validation.Valid;

@RestController
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public AuthController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody @Valid UserInfo userInfo) {
        UserEntity u = new UserEntity();
        u.setPassword(userInfo.getPassword());
        u.setLogin(userInfo.getLogin());
        userService.saveUser(u);
        return "OK";
    }

    @PostMapping("/auth")
    public ResponseEntity<String> auth(@RequestBody UserInfo request) {
        UserEntity userEntity = userService.findByLoginAndPassword(request.getLogin(), request.getPassword());
        String token = jwtProvider.generateToken(userEntity.getLogin());
        return new ResponseEntity<>(token, HttpStatus.OK);
    }
}

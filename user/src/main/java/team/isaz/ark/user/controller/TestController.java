package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @GetMapping("/admin/get")
    public String getAdmin() {
        return "Hi admin";
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/user/get")
    public String getUser() {
        return "Hi user";
    }
}

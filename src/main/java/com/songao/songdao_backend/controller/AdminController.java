package com.songao.songdao_backend.controller;


import com.songao.songdao_backend.exception.LoginException;
import com.songao.songdao_backend.exception.LogoutException;
import com.songao.songdao_backend.exception.RegisterException;
import com.songao.songdao_backend.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "*")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String email, @RequestParam String password) {
        try {
            adminService.addNewAdmin(email, password);
            return new ResponseEntity<>("Registrierung erfolgreich.", HttpStatus.CREATED);
        } catch (RegisterException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        try {
            adminService.login(email, password, session);
            return new ResponseEntity<>("Anmeldung erfolgreich.", HttpStatus.OK);
        } catch (LoginException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(HttpSession session) {
        try {
            adminService.logout(session);
            return new ResponseEntity<>("Abmeldung erfolgreich.", HttpStatus.OK);
        } catch (LogoutException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}

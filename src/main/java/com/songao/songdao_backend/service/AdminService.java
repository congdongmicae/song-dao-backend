package com.songao.songdao_backend.service;

import com.songao.songdao_backend.exception.LoginException;
import com.songao.songdao_backend.exception.LogoutException;
import com.songao.songdao_backend.model.Admin;
import com.songao.songdao_backend.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void addNewAdmin(String email, String password) {
        Admin newAdmin = new Admin();
        newAdmin.setEmail(email);
        newAdmin.setPassword(passwordEncoder.encode(password));
        adminRepository.save(newAdmin);
    }

    public void login(String email, String password, HttpSession session) {
            Optional<Admin> registeredAdmin = adminRepository.findAdminByEmail(email);
            if (registeredAdmin.isPresent() && passwordEncoder.matches(password, registeredAdmin.get().getPassword())) {
                session.setAttribute("loggedAdmin", registeredAdmin.get());
            } else {
                throw new LoginException("Anmeldung fehlgeschlagen.");
            }
    }

    public void logout(HttpSession session) {
        if (session == null) {
            throw new LogoutException("Abmeldung fehlgeschlagen.");
        }

        session.invalidate();
    }
}


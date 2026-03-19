package com.clinic.system.service;

import com.clinic.system.dto.request.LoginRequestDTO;
import com.clinic.system.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO dto);

    AuthResponseDTO register(String username, String email, String password, String fullName);

    void logout(String username);
}


package com.example.Stq.autenticacao.application;

import com.example.Stq.autenticacao.application.dto.LoginRequest;
import com.example.Stq.autenticacao.application.dto.LoginResponse;
import com.example.Stq.autenticacao.application.dto.RefreshRequest;

public interface AutenticacaoService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshRequest request);
}

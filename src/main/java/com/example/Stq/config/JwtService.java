package com.example.Stq.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.exception.CredenciaisInvalidasException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_ACCESS = "access";
    private static final String TIPO_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public String gerarAccessToken(Usuario usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim("perfil", usuario.getPerfil().name())
                .withClaim(CLAIM_TIPO, TIPO_ACCESS)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(jwtProperties.getAccessTokenExpiration()))
                .sign(algoritmo());
    }

    public String gerarRefreshToken(Usuario usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim(CLAIM_TIPO, TIPO_REFRESH)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiration()))
                .sign(algoritmo());
    }

    public String extrairEmailDoAccessToken(String token) {
        return verificarEDecodificar(token, TIPO_ACCESS).getSubject();
    }

    public String extrairEmailDoRefreshToken(String token) {
        return verificarEDecodificar(token, TIPO_REFRESH).getSubject();
    }

    public long getAccessTokenExpiration() {
        return jwtProperties.getAccessTokenExpiration();
    }

    private DecodedJWT verificarEDecodificar(String token, String tipoEsperado) {
        try {
            DecodedJWT decoded = JWT.require(algoritmo()).build().verify(token);
            String tipo = decoded.getClaim(CLAIM_TIPO).asString();
            if (!tipoEsperado.equals(tipo)) {
                throw new CredenciaisInvalidasException();
            }
            return decoded;
        } catch (JWTVerificationException e) {
            throw new CredenciaisInvalidasException();
        }
    }

    private Algorithm algoritmo() {
        return Algorithm.HMAC256(jwtProperties.getSecret());
    }
}

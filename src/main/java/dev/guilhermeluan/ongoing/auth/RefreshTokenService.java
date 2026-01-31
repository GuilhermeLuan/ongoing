package dev.guilhermeluan.ongoing.auth;

import dev.guilhermeluan.ongoing.exception.InvalidCredentialException;
import dev.guilhermeluan.ongoing.user.RefreshToken;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteTokenInNewTransaction(Long tokenId) {
        refreshTokenRepository.deleteById(tokenId);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByTokenOrThrowInvalidCredentialException(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidCredentialException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));
    }

    public boolean isExpired(RefreshToken token) {
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }
}

package de.zorro909.discordnotifier.security;

import de.zorro909.discordnotifier.config.UserConfig;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class BasicPasswordAuthenticator implements AuthenticationProvider {

    private final UserConfig userConfig;

    private SecretKeyFactory factory;

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        // Authenticate User via Password PBKDF2 in userConfig
        return userConfig
                .getUser(authenticationRequest.getIdentity().toString())
                .flatMap(user -> {
                    String password = authenticationRequest.getSecret().toString();
                    // Validate User password with PBKDF2
                    KeySpec spec =
                            new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(user.getSalt()), 65536,
                                           128);
                    return generateSecret(spec).map(secret -> {
                        byte[] encoded = secret.getEncoded();
                        byte[] userPassword = Base64.getDecoder().decode(user.getPassword());
                        if (Arrays.equals(encoded, userPassword)) {
                            return user;
                        }
                        return null;
                    });
                })
                .map(user -> Flux.just(AuthenticationResponse.success(user.getName())))
                .orElseGet(() -> Flux.just(AuthenticationResponse.failure()));
    }

    private Optional<SecretKey> generateSecret(KeySpec keySpec) {
        try {
            return Optional.of(getFactory().generateSecret(keySpec));
        } catch (InvalidKeySpecException e) {
            log.warn("Invalid KeySpec", e);
            return Optional.empty();
        }
    }

    private SecretKeyFactory getFactory() {
        if (factory == null) {
            try {
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return factory;
    }
}

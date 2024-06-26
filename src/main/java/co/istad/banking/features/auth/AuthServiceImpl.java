package co.istad.banking.features.auth;

import co.istad.banking.domain.User;
import co.istad.banking.features.auth.dto.AuthResponse;
import co.istad.banking.features.auth.dto.ChangePasswordRequest;
import co.istad.banking.features.auth.dto.LoginRequest;
import co.istad.banking.features.auth.dto.RefreshTokenRequest;
import co.istad.banking.features.token.TokenService;
import co.istad.banking.features.user.UserRepository;
import co.istad.banking.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final JwtEncoder jwtEncoder;
    private final TokenService tokenService;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private JwtEncoder refreshJwtEncoder;

    @Qualifier("refreshJwtEncoder")
    @Autowired
    public void setRefreshJwtEncoder(JwtEncoder refreshJwtEncoder){
        this.refreshJwtEncoder = refreshJwtEncoder;
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {

        Authentication auth = new BearerTokenAuthenticationToken(
                request.refreshToken()
        );

        auth = jwtAuthenticationProvider.authenticate(auth);

        return tokenService.createToken(auth);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest, Jwt jwt) {
        User user = userRepository.findByPhoneNumber(jwt.getId())
                .orElseThrow(()->
                         new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        if(!changePasswordRequest.password().equals(changePasswordRequest.confirmedPassword())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is not match"
            );
        }

        if(!passwordEncoder.matches(changePasswordRequest.oldPassword(), user.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password does not match"
            );
        }

        if(passwordEncoder.matches(changePasswordRequest.password(), user.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is not allowed"
            );
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.password()));
        userRepository.save(user);

    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginRequest.phoneNumber(),
                loginRequest.password()
        );

        auth = daoAuthenticationProvider.authenticate(auth);

        return tokenService.createToken(auth);
    }

    /*@Override
    public AuthResponse login(LoginRequest loginRequest) {
        // Auth with DTO
        Authentication auth  = new UsernamePasswordAuthenticationToken(
                loginRequest.phoneNumber(),
                loginRequest.password()
        );

        auth = daoAuthenticationProvider.authenticate(auth);
        log.info("Auth :"+auth.getPrincipal());

        CustomUserDetails customUserDetail = (CustomUserDetails) auth.getPrincipal();
        log.info(customUserDetail.getUsername());
        log.info(customUserDetail.getUser().getName());
        customUserDetail.getAuthorities()
                        .forEach(grantedAuthority ->
                                System.out.println(grantedAuthority.getAuthority()));

        String scope = customUserDetail.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                //.filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.joining());
        log.info("Scope {}", scope);

        Instant now = Instant.now();

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .id(customUserDetail.getUsername())
                .subject("Access Resource")
                .audience(List.of("WEB","MOBILE"))
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.SECONDS))
                .issuer(customUserDetail.getUsername())
                .claim("scope",scope)
                .build();

        JwtClaimsSet refreshJwtClaimsSet = JwtClaimsSet.builder()
                .id(customUserDetail.getUsername())
                .subject("Refresh Resource")
                .audience(List.of("WEB","MOBILE"))
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .issuer(customUserDetail.getUsername())
                .claim("scope",scope)
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        String refreshToken = refreshJwtEncoder.encode(JwtEncoderParameters.from(refreshJwtClaimsSet)).getTokenValue();

        return new AuthResponse(
                "Bearer",
                accessToken,
                refreshToken
        );

    }*/

}

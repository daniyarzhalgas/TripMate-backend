package kz.sdu.service;

import kz.sdu.dto.*;
import kz.sdu.entity.EmailVerificationCode;
import kz.sdu.entity.PasswordResetToken;
import kz.sdu.entity.UserProfile;
import kz.sdu.keycloak.KeycloakAdminFactory;
import kz.sdu.keycloak.OidcTokenClient;
import kz.sdu.repository.EmailVerificationCodeRepository;
import kz.sdu.repository.PasswordResetTokenRepository;
import kz.sdu.repository.UserProfileRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private static final SecureRandom RNG = new SecureRandom();

    private final OidcTokenClient oidcTokenClient;
    private final KeycloakAdminFactory keycloakAdminFactory;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailService emailService;

    public UserService(
            OidcTokenClient oidcTokenClient,
            KeycloakAdminFactory keycloakAdminFactory,
            EmailVerificationCodeRepository emailVerificationCodeRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserProfileRepository userProfileRepository,
            EmailService emailService
    ) {
        this.oidcTokenClient = oidcTokenClient;
        this.keycloakAdminFactory = keycloakAdminFactory;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userProfileRepository = userProfileRepository;
        this.emailService = emailService;
    }

    public UserLoginResponseDto login(UserLoginRequestDto req) {
        try {
            Map<String, Object> tokens = oidcTokenClient.passwordGrant(req.getEmail(), req.getPassword());
            String accessToken = (String) tokens.get("access_token");
            String refreshToken = (String) tokens.get("refresh_token");

            UserRepresentation user = findKeycloakUserByEmail(req.getEmail());
            ensureUserProfileIfMissing(user);

            UserProfile profile = userProfileRepository.findByEmail(req.getEmail()).orElse(null);
            return UserLoginResponseDto.builder()
                    .success(true)
                    .data(UserLoginResponseDataDto.builder()
                            .user(toAuthUser(user, profile, false))
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build())
                    .build();
        } catch (HttpClientErrorException e) {
            return UserLoginResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("INVALID_CREDENTIALS")
                            .message("Invalid email or password")
                            .build())
                    .build();
        }
    }

    @Transactional
    public RegisterResponseDto register(UserRegistrationDto req) {
        UserRepresentation existing = findKeycloakUserByEmailOrNull(req.getEmail());
        if (existing != null) {
            return RegisterResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("EMAIL_EXISTS")
                            .message("Email already registered")
                            .build())
                    .build();
        }

        UUID userId = createKeycloakUserDisabled(req);
        ensureUserProfileById(userId, req);

        String code = generateCode6();
        emailVerificationCodeRepository.save(EmailVerificationCode.builder()
                .email(req.getEmail())
                .code(code)
                .rawPassword(req.getPassword())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());
        emailService.sendVerificationCode(req.getEmail(), code);

        return RegisterResponseDto.builder()
                .success(true)
                .data(RegisterResponseDataDto.builder()
                        .userId(userId)
                        .email(req.getEmail())
                        .verificationRequired(true)
                        .message("Verification code sent to email")
                        .build())
                .build();
    }

    @Transactional
    public VerifyEmailResponseDto verifyEmail(VerifyEmailRequestDto req) {
        EmailVerificationCode code = emailVerificationCodeRepository.findById(req.getEmail())
                .orElse(null);
        if (code == null || code.getExpiresAt().isBefore(LocalDateTime.now()) || !code.getCode().equals(req.getCode())) {
            return VerifyEmailResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("INVALID_CODE")
                            .message("Invalid or expired verification code")
                            .build())
                    .build();
        }

        UserRepresentation user = findKeycloakUserByEmail(req.getEmail());
        enableAndVerifyEmail(user.getId());
        emailVerificationCodeRepository.deleteById(req.getEmail());

        Map<String, Object> tokens = oidcTokenClient.passwordGrant(req.getEmail(), code.getRawPassword());
        return VerifyEmailResponseDto.builder()
                .success(true)
                .data(VerifyEmailResponseDataDto.builder()
                        .verified(true)
                        .accessToken((String) tokens.get("access_token"))
                        .refreshToken((String) tokens.get("refresh_token"))
                        .build())
                .build();
    }

    @Transactional
    public SimpleMessageResponseDto resendVerification(ResendVerificationRequestDto req) {
        EmailVerificationCode existing = emailVerificationCodeRepository.findById(req.getEmail()).orElse(null);
        if (existing == null) {
            return SimpleMessageResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("VERIFICATION_NOT_PENDING")
                            .message("No pending verification for this email")
                            .build())
                    .build();
        }

        String newCode = generateCode6();
        existing.setCode(newCode);
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        emailVerificationCodeRepository.save(existing);
        emailService.sendVerificationCode(req.getEmail(), newCode);

        return SimpleMessageResponseDto.builder()
                .success(true)
                .message("Verification code sent")
                .build();
    }

    @Transactional
    public SimpleMessageResponseDto forgotPassword(ForgotPasswordRequestDto req) {
        // Always 200 per your contract
        UserRepresentation user = findKeycloakUserByEmailOrNull(req.getEmail());
        if (user != null) {
            UUID token = UUID.randomUUID();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .token(token)
                    .email(req.getEmail())
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build());
            emailService.sendPasswordResetLink(req.getEmail(), token.toString());
        }

        return SimpleMessageResponseDto.builder()
                .success(true)
                .message("Password reset link sent to email")
                .build();
    }

    @Transactional
    public SimpleMessageResponseDto resetPassword(ResetPasswordRequestDto req) {
        UUID token;
        try {
            token = UUID.fromString(req.getToken());
        } catch (IllegalArgumentException e) {
            return SimpleMessageResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("INVALID_TOKEN")
                            .message("Invalid reset token")
                            .build())
                    .build();
        }

        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token).orElse(null);
        if (prt == null || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            return SimpleMessageResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("INVALID_TOKEN")
                            .message("Invalid reset token")
                            .build())
                    .build();
        }

        UserRepresentation user = findKeycloakUserByEmail(prt.getEmail());
        setPassword(user.getId(), req.getNewPassword());
        passwordResetTokenRepository.deleteById(token);

        return SimpleMessageResponseDto.builder()
                .success(true)
                .message("Password reset successful")
                .build();
    }

    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto req) {
        try {
            Map<String, Object> tokens = oidcTokenClient.refreshToken(req.getRefreshToken());
            return RefreshTokenResponseDto.builder()
                    .success(true)
                    .data(RefreshTokenResponseDataDto.builder()
                            .accessToken((String) tokens.get("access_token"))
                            .refreshToken((String) tokens.get("refresh_token"))
                            .build())
                    .build();
        } catch (HttpClientErrorException e) {
            return RefreshTokenResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("INVALID_REFRESH_TOKEN")
                            .message("Invalid refresh token")
                            .build())
                    .build();
        }
    }

    public SimpleMessageResponseDto logout(LogoutRequestDto req) {
        try {
            oidcTokenClient.logout(req.getRefreshToken());
        } catch (Exception ignored) {
        }
        return SimpleMessageResponseDto.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    public UserLoginResponseDto google(GoogleAuthRequestDto req) {
        try {
            Map<String, Object> tokens = oidcTokenClient.tokenExchangeGoogle(req.getIdToken());
            return UserLoginResponseDto.builder()
                    .success(true)
                    .data(UserLoginResponseDataDto.builder()
                            .user(AuthUserDto.builder()
                                    .id(null)
                                    .email(null)
                                    .name(null)
                                    .isNewUser(false)
                                    .profileComplete(false)
                                    .build())
                            .accessToken((String) tokens.get("access_token"))
                            .refreshToken((String) tokens.get("refresh_token"))
                            .build())
                    .build();
        } catch (Exception e) {
            return UserLoginResponseDto.builder()
                    .success(false)
                    .error(ApiErrorDto.builder()
                            .code("GOOGLE_AUTH_FAILED")
                            .message("Google authentication failed")
                            .build())
                    .build();
        }
    }

    private UsersResource users() {
        Keycloak kc = keycloakAdminFactory.admin();
        return kc.realm("tripmate").users();
    }

    private UserRepresentation findKeycloakUserByEmail(String email) {
        UserRepresentation user = findKeycloakUserByEmailOrNull(email);
        if (user == null) throw new RuntimeException("USER_NOT_FOUND");
        return user;
    }

    private UserRepresentation findKeycloakUserByEmailOrNull(String email) {
        List<UserRepresentation> list = users().searchByEmail(email, true);
        return list == null || list.isEmpty() ? null : list.getFirst();
    }

    private UUID createKeycloakUserDisabled(UserRegistrationDto req) {
        UserRepresentation u = new UserRepresentation();
        u.setEmail(req.getEmail());
        u.setUsername(req.getEmail());
        u.setEnabled(false);
        u.setEmailVerified(false);
        u.setFirstName(req.getFirstname());
        u.setLastName(req.getLastname());

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        cred.setValue(req.getPassword());
        u.setCredentials(List.of(cred));

        var response = users().create(u);
        try {
            String location = response.getLocation() != null ? response.getLocation().toString() : null;
            if (location == null) throw new RuntimeException("CREATE_FAILED");
            String id = location.substring(location.lastIndexOf('/') + 1);
            return UUID.fromString(id);
        } finally {
            response.close();
        }
    }

    private void enableAndVerifyEmail(String userId) {
        UserRepresentation rep = users().get(userId).toRepresentation();
        rep.setEnabled(true);
        rep.setEmailVerified(true);
        users().get(userId).update(rep);
    }

    private void setPassword(String userId, String newPassword) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        cred.setValue(newPassword);
        users().get(userId).resetPassword(cred);
    }

    private void ensureUserProfileIfMissing(UserRepresentation user) {
        if (user == null || user.getId() == null) return;
        UUID id;
        try {
            id = UUID.fromString(user.getId());
        } catch (Exception e) {
            return;
        }
        if (userProfileRepository.existsById(id)) return;
        userProfileRepository.save(UserProfile.builder()
                .id(id)
                .email(user.getEmail())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .dateOfBirth(null)
                .gender(null)
                .profileComplete(false)
                .build());
    }

    private void ensureUserProfileById(UUID id, UserRegistrationDto req) {
        if (userProfileRepository.existsById(id)) return;
        LocalDate dob = parseDate(req.getDateOfBirth());
        userProfileRepository.save(UserProfile.builder()
                .id(id)
                .email(req.getEmail())
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .dateOfBirth(dob)
                .gender(req.getGender())
                .profileComplete(false)
                .build());
    }

    private AuthUserDto toAuthUser(UserRepresentation user, UserProfile profile, boolean isNewUser) {
        UUID id = null;
        try {
            id = user.getId() != null ? UUID.fromString(user.getId()) : null;
        } catch (Exception ignored) {
        }
        String name = profile != null && profile.getFirstname() != null ? profile.getFirstname() : user.getFirstName();
        boolean profileComplete = profile != null && profile.isProfileComplete();
        return AuthUserDto.builder()
                .id(id)
                .email(user.getEmail())
                .name(name)
                .isNewUser(isNewUser)
                .profileComplete(profileComplete)
                .build();
    }

    private static String generateCode6() {
        int v = RNG.nextInt(1_000_000);
        return String.format("%06d", v);
    }

    private static LocalDate parseDate(String yyyyMmDd) {
        try {
            return LocalDate.parse(yyyyMmDd);
        } catch (Exception e) {
            return null;
        }
    }
}

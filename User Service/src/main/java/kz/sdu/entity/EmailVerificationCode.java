package kz.sdu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationCode {

    @Id
    @Column(nullable = false, updatable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    /**
     * Temporary storage to allow issuing tokens on /verify-email (per your contract).
     * In production you should avoid storing plaintext passwords.
     */
    @Column(nullable = false)
    private String rawPassword;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}


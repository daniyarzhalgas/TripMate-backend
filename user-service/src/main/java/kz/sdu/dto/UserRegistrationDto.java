package kz.sdu.dto;

import kz.sdu.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String firstname;
    @NotBlank
    private String lastname;

    @NotBlank
    private String dateOfBirth;
    private Gender gender;
}

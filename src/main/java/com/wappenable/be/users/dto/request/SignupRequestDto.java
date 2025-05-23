package com.wappenable.be.users.dto.request;

import com.wappenable.be.users.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor // JSON 역직렬화를 위해 필요
@AllArgsConstructor // 테스트나 내부에서 생성자 주입할 때 사용
public class SignupRequestDto {
    
    @NotBlank(message = "아이디 입력은 필수입니다.")
    @Pattern(
        regexp = "^[a-z0-9]{4,20}$",
        message = "아이디는 영문 소문자와 숫자만 포함한 4~20자여야 합니다."
    )
    private String email;

    @NotBlank(message = "이메일 입력은 필수입니다.")
    @Email(message = "이메일이 올바르지 않습니다.")
    private String recoveryEmail;
    
    // TODO : PM은 nickname 두라고 함 / 프론트에서는 nickname란 없음. 일단 냅두고 User 도메인에서 nickname 필드를 null 허용으로 두었음.
    // @NotBlank(message = "닉네임 입력은 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9]{2,20}$", message = "닉네임은 영어 또는 숫자만 포함한 2~20자여야 합니다.") // 공백 방어
    private String nickname;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
    private String password;
    
    // [x] : 비밀번호 재확인용
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;

    private Role role;
}


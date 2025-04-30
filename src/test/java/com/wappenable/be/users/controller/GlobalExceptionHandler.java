// package com.wappenable.be.users.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.wappenable.be.users.dto.SignupRequest;
// import com.wappenable.be.users.exception.CustomException;
// import com.wappenable.be.users.service.UserService;

// import jakarta.transaction.Transactional;

// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.mockito.Mockito.doNothing;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

// @SpringBootTest
// @Transactional // 테스트 끝나면 DB 롤백
// // @WebMvcTest(UserController.class) // ✅ Controller만 테스트
// // 실제로는 절대 쓰면 안된다. Spring Security가 테스트 중에 인증/권한/CSRF 검사 다 꺼버려서
// // 요청이 무조건 컨트롤러까지 도달한다.
// @AutoConfigureMockMvc(addFilters = false)
// class UserControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private UserService userService; // ✅ Service는 가짜(Mock)로

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Test
//     @DisplayName("회원가입 성공")
//     @AutoConfigureMockMvc(printOnlyOnFailure = false) // 테스트 여부와 관계 없이 andDo 실행 시켜줌
//     void signup_success() throws Exception {
//         SignupRequest request = new SignupRequest();
//         request.setEmail("test@example.com");
//         request.setNickname("tester");
//         request.setPassword("password123");
//         request.setRole("USER");

//         // ✅ userService.signup()이 호출될 때 아무 동작 안 하게 설정
//         // doNothing().when(userService).signup(Mockito.any(SignupRequest.class));

//         mockMvc.perform(
//             post("/api/users/signup")
//                     .with(csrf())  // ✅ 여기
//                     .contentType(MediaType.APPLICATION_JSON)
//                     .content(objectMapper.writeValueAsString(request))
//         )
//         .andDo(print())
//         .andExpect(status().isOk());
//     }

//     @Test
//     @DisplayName("중복 이메일로 회원가입 실패 (409 Conflict)")
//     @AutoConfigureMockMvc(printOnlyOnFailure = false)
//     void signup_duplicateEmail() throws Exception {
//         SignupRequest request = new SignupRequest();
//         request.setEmail("duplicate@example.com");
//         request.setNickname("dup");
//         request.setPassword("password123");
//         request.setRole("USER");

//         // ✅ 중복 이메일이면 예외 던지게 설정
//         Mockito.doThrow(new CustomException("중복된 이메일입니다", org.springframework.http.HttpStatus.CONFLICT))
//                 .when(userService).signup(Mockito.any(SignupRequest.class));

//         mockMvc.perform(post("/api/users/signup")
//                         .with(csrf())  // ✅ 여기
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andDo(print())
//                 .andExpect(status().isConflict());
//     }
// }

// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     @ExceptionHandler(CustomException.class)
//     public ResponseEntity<String> handleCustomException(CustomException e) {
//         return ResponseEntity.status(e.getStatus()).body(e.getMessage());
//     }
// }

package com.wappenable.be.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wappenable.be.users.dto.SignupRequest;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공 - DB 저장 확인")
    void signup_success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setNickname("tester");
        request.setPassword("password123");
        request.setRole("USER");

        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패 - DB 저장 안됨")
    void signup_duplicateEmail() throws Exception {
        User existingUser = User.builder()
                .email("duplicate@example.com")
                .nickname("dup")
                .passwordHash("password123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(existingUser);

        SignupRequest request = new SignupRequest();
        request.setEmail("duplicate@example.com");
        request.setNickname("newdup");
        request.setPassword("newpassword123");
        request.setRole("USER");

        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict());

        long count = userRepository.findAll().stream()
            .filter(u -> u.getEmail().equals("duplicate@example.com"))
            .count();
        assertThat(count).isEqualTo(1);
    }
}

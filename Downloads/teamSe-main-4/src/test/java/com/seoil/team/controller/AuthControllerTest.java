package com.seoil.team.controller;//package com.seoil.team.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.seoil.team.constant.ResponseMessages;
//import com.seoil.team.domain.member.RoleType;
//import com.seoil.team.dto.request.Auth.LoginRequest;
//import com.seoil.team.dto.request.Auth.SignupRequest;
//import com.seoil.team.dto.response.Auth.UserInfoResponse;
//import com.seoil.team.security.jwt.JwtTokenProvider;
//import com.seoil.team.service.AuthService;
//import com.seoil.team.service.MemberService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(AuthController.class)
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private AuthenticationManager authenticationManager;
//
//    @MockBean
//    private JwtTokenProvider tokenProvider;
//
//    @MockBean
//    private MemberService memberService;
//
//    @MockBean
//    private AuthService authService;
//
//    private SignupRequest signupRequest;
//    private LoginRequest loginRequest;
//
//    @BeforeEach
//    void setUp() {
//        signupRequest = new SignupRequest("John Doe", "john@example.com", "password123", RoleType.USER);
//        loginRequest = new LoginRequest("john@example.com", "password123");
//    }
//
//    @Test
//    void registerUser_ShouldReturnOk() throws Exception {
//        mockMvc.perform(post("/api/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(signupRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().string(ResponseMessages.SUCCESS_USER_REGISTERED));
//    }
//
//    @Test
//    void authenticateUser_ShouldReturnJwtToken() throws Exception {
//        String token = "mock.jwt.token";
//        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(token);
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").value(token));
//    }
//
//    @Test
//    @WithMockUser(username = "john@example.com")
//    void getUserInfo_ShouldReturnUserInfo() throws Exception {
//        UserInfoResponse userInfoResponse = new UserInfoResponse("John Doe", "john@example.com", RoleType.USER);
//        when(authService.getUserInfo(any(Authentication.class))).thenReturn(userInfoResponse);
//
//        mockMvc.perform(get("/api/auth/user"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("John Doe"))
//                .andExpect(jsonPath("$.email").value("john@example.com"))
//                .andExpect(jsonPath("$.role").value("USER"));
//    }
//}
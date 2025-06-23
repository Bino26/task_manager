package com.taskmanager.taskmanager.unit.service;

import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.service.UserDetailsServiceImpl;
import com.taskmanager.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Set.of(Role.TEAM_MEMBER));
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("TEAM_MEMBER")));

        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        when(userService.findByEmail("notfound@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found with email: notfound@example.com" ));

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("notfound@example.com"));

        verify(userService, times(1)).findByEmail("notfound@example.com");
    }
}

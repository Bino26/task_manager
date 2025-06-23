package com.taskmanager.taskmanager.entity;

import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;


import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "deleted_at"})
})
public class User extends BaseEntity {

    @Column(nullable = false)
    private String nomUtilisateur;

    @Column(nullable = false)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus statut = UserStatus.ACTIF;


    @Column(nullable = false)
    private String password;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @JoinTable(name = "authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> role;


}

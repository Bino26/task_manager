package com.taskmanager.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    @JsonIgnore
    User user;

    @Column(nullable = false, unique = true)
    String token;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date expiryDate;
}

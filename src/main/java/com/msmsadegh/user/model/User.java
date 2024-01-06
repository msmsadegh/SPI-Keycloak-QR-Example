package com.msmsadegh.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usr")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String nationalCode;

    @NotNull
    @Column(unique = true)
    private String phoneNumber;

    @NotNull
    @Builder.Default
    private Boolean isConfirm = false;

    private String verificationCode;
    private Instant removedAt;
    private Instant createdAt;
    private Instant messageSentAt;
}
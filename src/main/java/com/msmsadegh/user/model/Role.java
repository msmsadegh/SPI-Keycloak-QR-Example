package com.msmsadegh.user.model;

import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.mop.account.common.model.Api;
import org.mop.account.common.model.Menu;
import org.mop.account.user.RoleType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Unique
    @Enumerated(EnumType.STRING)
    private RoleType enumName;

    @NotNull
    @Unique
    private String faName;

    @NotNull
    @Unique
    private String enName;

    @ManyToMany
    @JoinTable(
            name = "api_access",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "api_id"))
    private List<Api> apis;

    @ManyToMany
    @JoinTable(
            name = "menu_access",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id"))
    private List<Menu> menus;
}
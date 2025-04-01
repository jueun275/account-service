package com.example.account.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Entity
public class AccountUser extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String userName;

    public AccountUser(String userName) {
        this.userName = userName;
    }

    @Builder
    public AccountUser(Long id, String userName) {
        this.id = id;
        this.userName = userName;
    }
}

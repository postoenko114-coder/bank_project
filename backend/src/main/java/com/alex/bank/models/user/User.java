package com.alex.bank.models.user;

import com.alex.bank.models.account.Account;
import com.alex.bank.models.branch.reservation.Reservation;
import com.alex.bank.models.card.Card;
import com.alex.bank.models.notification.Notification;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private RoleUser roleUser;

    @Column(unique = true)
    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Card> cards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Notification> notifications;

    private LocalDateTime createdAt;

    @Override
    public String getPassword() {return password;}

    //For userDetailsService
    @Override
    public String getUsername() {return email;}

    //For business-logic
    public String getRealUsername() {return username;}

    @Override
    public boolean isAccountNonExpired() {return UserDetails.super.isAccountNonExpired();}

    @Override
    public boolean isAccountNonLocked() {return UserDetails.super.isAccountNonLocked();}

    @Override
    public boolean isCredentialsNonExpired() {return UserDetails.super.isCredentialsNonExpired();}

    @Override
    public boolean isEnabled() {return UserDetails.super.isEnabled();}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleUser.name()));
    }

}

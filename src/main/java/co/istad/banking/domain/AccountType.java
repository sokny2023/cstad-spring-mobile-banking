package co.istad.banking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "account_type")
public class AccountType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String alias;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean isDeleted;

    @OneToMany(mappedBy = "accountType")
    private List<Account> accounts;
}
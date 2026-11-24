package org.talentlink.talentlink.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter @Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "industry", length = 120)
    private String industry;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
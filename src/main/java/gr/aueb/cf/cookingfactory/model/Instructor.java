package gr.aueb.cf.cookingfactory.model;


import gr.aueb.cf.cookingfactory.core.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "instructors")
public class Instructor extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @ColumnDefault("true")
    private Boolean isActive;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "instructor")
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

    public Set<Course> getAllCourses() {
        if (courses == null) courses = new HashSet<>();
        return Collections.unmodifiableSet(courses);
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "contact_details_id")
    private ContactDetails contactDetails;

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
    }
}


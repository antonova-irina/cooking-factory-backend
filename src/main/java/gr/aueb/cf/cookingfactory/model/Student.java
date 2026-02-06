package gr.aueb.cf.cookingfactory.model;

import gr.aueb.cf.cookingfactory.core.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "students")
public class Student extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uuid;

    @ColumnDefault("true")
    private Boolean isActive;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, unique = true)
    private String vat;

    @Column(unique = true)
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "contact_details_id")
    private ContactDetails contactDetails;

    @Getter(AccessLevel.PROTECTED)
    @ManyToMany
    @JoinTable(
            name = "students_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    public Set<Course> getAllCourses() {
        if (courses == null) courses = new HashSet<>();
        return Collections.unmodifiableSet(courses);
    }

    public void addCourses(Course course) {
        if (courses == null) courses = new HashSet<>();
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.getStudents().remove(this);
    }

    public boolean hasCourses(){
        return courses != null && !courses.isEmpty();
    }

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
    }
}

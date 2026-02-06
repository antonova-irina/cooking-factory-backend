package gr.aueb.cf.cookingfactory.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("true")
    private Boolean isActive;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @Getter(AccessLevel.PROTECTED)
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private Set<Student> students = new HashSet<>();

    public Set<Student> getAllStudents() {
        return Collections.unmodifiableSet(students);
    }

    public void addStudent(Student student) {
        if (students == null) students = new HashSet<>();
        students.add(student);
        student.getCourses().add(this);
    }

    public void removeStudent(Student student) {
        if (students == null) return;
        students.remove(student);
        student.getCourses().remove(this);
    }
}

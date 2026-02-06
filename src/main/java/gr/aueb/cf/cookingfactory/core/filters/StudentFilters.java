package gr.aueb.cf.cookingfactory.core.filters;

import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class StudentFilters extends GenericFilters{
    @Nullable
    private String lastname;

    @Nullable
    private LocalDate dateOfBirth;

    @Nullable
    private Long courseId;

    @Nullable
    private String instructorUuid;
}

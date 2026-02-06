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
public class InstructorFilters extends GenericFilters {
    @Nullable
    private String uuid;

    @Nullable
    private String lastname;
}

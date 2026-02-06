package gr.aueb.cf.cookingfactory.core.filters;

import jakarta.annotation.Nullable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CourseFilters extends GenericFilters  {
    @Nullable
    private Long id;
    @Nullable
    private String name;
    @Nullable
    private Long instructorId;
}

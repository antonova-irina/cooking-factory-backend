package gr.aueb.cf.cookingfactory.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CourseInsertDTO(
        @NotNull(message = "isActive field is required")
        Boolean isActive,

        @NotEmpty(message = "Course name is required")
        String name,

        @NotEmpty(message = "Description is required")
        String description,

        Long instructorId
) {}


package gr.aueb.cf.cookingfactory.dto;

import lombok.Builder;

@Builder
public record CourseReadOnlyDTO(
        Long id,
        Boolean isActive,
        String name,
        String description,
        Long instructorId
) {}

package gr.aueb.cf.cookingfactory.api;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.exceptions.ValidationException;
import gr.aueb.cf.cookingfactory.core.filters.CourseFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.service.ICourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseRestController {

    private final ICourseService courseService;

    @Operation(
            summary = "Add a course",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Course created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Course already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    )
                    ,
                    @ApiResponse(
                            responseCode = "404", description = "Instructor with given ID not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    )
            }
    )
    @PostMapping(value = "/courses")
    public ResponseEntity<CourseReadOnlyDTO> saveCourse(
            @Valid @RequestBody CourseInsertDTO courseInsertDTO,
            BindingResult bindingResult
    )   throws AppObjectAlreadyExistsException, AppObjectNotFoundException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        CourseReadOnlyDTO courseReadOnlyDTO = courseService.saveCourse(courseInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()                       // request URI is /courses
                .path("/{id}")                            // Appends "/{id}"
                .buildAndExpand(courseReadOnlyDTO.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(courseReadOnlyDTO);
    }


    @Operation(
            summary = "Get all courses",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Courses returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseReadOnlyDTO.class))),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class)))
            }
    )
    @GetMapping("/courses/getAllCourses")
    public ResponseEntity<List<CourseReadOnlyDTO>> getAllCourses() {
        List<CourseReadOnlyDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }


    @Operation(
            summary = "Get courses paginated and optionally filtered", // Use body {} or { \"page\": 0, \"pageSize\": 10 } for simple pagination; add name, instructorId for filters.
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Courses returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Paginated.class,
                                    example = """
                                                {
                                                  "data": [
                                                    { "id": 1, "isActive": true, "name": "...", "description": "...", "instructorId": 1 },
                                                    { "id": 2, "isActive": true, "name": "...", "description": "...", "instructorId": null }
                                                  ],
                                                  "currentPage": 2,
                                                  "pageSize": 10,
                                                  "totalPages": 5,
                                                  "numberOfElements": 7,
                                                  "totalElements": 50
                                                }"""
                            ))
                    ),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class)))
            }
    )
    @PostMapping("/courses/search")
    public ResponseEntity<Paginated<CourseReadOnlyDTO>> getFilteredAndPaginatedCourses(
            @Nullable @RequestBody CourseFilters filters)  {

        if (filters == null) filters = CourseFilters.builder().build();
        Paginated<CourseReadOnlyDTO> dtoPaginated = courseService.getCoursesFilteredPaginated(filters);
        return ResponseEntity.ok(dtoPaginated);
    }

    @Operation(
            summary = "Get one course by id",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Course returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Course not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    )
            }
    )
    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseReadOnlyDTO> getCourseById(@PathVariable Long id)
            throws AppObjectNotFoundException {
        return ResponseEntity.ok(courseService.getOneCourse(id));
    }


    @Operation(
            summary = "Update a course",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Course updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Course already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Course or Instructor not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    )
            }
    )
    @PutMapping(value = "/courses/{id}")
    @PreAuthorize("#id == #courseUpdateDTO.id()")  // AccessDeniedException
    public ResponseEntity<CourseReadOnlyDTO> updateCourse(@PathVariable Long id,
                                                                  @Valid @RequestBody CourseUpdateDTO courseUpdateDTO,
                                                                  BindingResult bindingResult)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        CourseReadOnlyDTO courseReadOnlyDTO = courseService.updateCourse(courseUpdateDTO);

        return ResponseEntity.ok(courseReadOnlyDTO);
    }

}

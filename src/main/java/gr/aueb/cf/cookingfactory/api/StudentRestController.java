package gr.aueb.cf.cookingfactory.api;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.exceptions.ValidationException;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.core.filters.StudentFilters;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.service.IStudentService;
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
public class StudentRestController {

    private final IStudentService studentService;

    @Operation(
            summary = "Add a student",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Student created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Student already exists",
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
                            responseCode = "404", description = "Course with given ID not found",
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
    @PostMapping(value = "/students")
    public ResponseEntity<StudentReadOnlyDTO> saveStudent(
            @Valid @RequestBody StudentInsertDTO studentInsertDTO,
            BindingResult bindingResult
    )   throws AppObjectAlreadyExistsException, AppObjectNotFoundException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        StudentReadOnlyDTO studentReadOnlyDTO = studentService.saveStudent(studentInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()                       // request URI is /students
                .path("/{uuid}")                            // Appends "/{uuid}"
                .buildAndExpand(studentReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(studentReadOnlyDTO);
    }


    @Operation(
            summary = "Get all students",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Students returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentReadOnlyDTO.class))),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class)))
            }
    )
    @GetMapping("/students/getAllStudents")
    public ResponseEntity<List<StudentReadOnlyDTO>> getAllStudents() {
        List<StudentReadOnlyDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }


    @Operation(
            summary = "Get students paginated and optionally filtered", // Use body {} or { \"page\": 0, \"pageSize\": 10 } for simple pagination; add lastname, dateOfBirth, courseId, instructorUuid for filters."
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Students returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Paginated.class,
                                    example = """
                                                {
                                                  "data": [
                                                    { "id": 1, "uuid": "...", ... },
                                                    { "id": 2, "uuid": "...", ... }
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
    @PostMapping("/students/search")
    public ResponseEntity<Paginated<StudentReadOnlyDTO>> getFilteredAndPaginatedStudents(
            @Nullable @RequestBody StudentFilters filters)  {

        if (filters == null) filters = StudentFilters.builder().build();
        Paginated<StudentReadOnlyDTO> dtoPaginated = studentService.getStudentsFilteredPaginated(filters);
        return ResponseEntity.ok(dtoPaginated);
    }


    @Operation(
            summary = "Get one student by uuid",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Student returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Student not found",
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
    @GetMapping("/students/{uuid}")
    public ResponseEntity<StudentReadOnlyDTO> getStudentByUuid(@PathVariable String uuid)
            throws AppObjectNotFoundException {
        return ResponseEntity.ok(studentService.getOneStudent(uuid));
    }


    @Operation(
            summary = "Update a student",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Student updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Student already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Student or Course not found",
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
    @PutMapping(value = "/students/{uuid}")
    @PreAuthorize("#uuid == #studentUpdateDTO.uuid()")  // AccessDeniedException
    public ResponseEntity<StudentReadOnlyDTO> updateStudent(@PathVariable String uuid,
                                                              @Valid @RequestBody StudentUpdateDTO studentUpdateDTO,
                                                              BindingResult bindingResult)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        StudentReadOnlyDTO studentReadOnlyDTO = studentService.updateStudent(studentUpdateDTO);

        return ResponseEntity.ok(studentReadOnlyDTO);
    }
}


package gr.aueb.cf.cookingfactory.api;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.exceptions.ValidationException;
import gr.aueb.cf.cookingfactory.core.filters.InstructorFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.service.IInstructorService;
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
public class InstructorRestController {

    private final IInstructorService instructorService;

    @Operation(
            summary = "Add an instructor",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Instructor created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InstructorReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Instructor already exists",
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
    @PostMapping(value = "/instructors")
    public ResponseEntity<InstructorReadOnlyDTO> saveInstructor(
            @Valid @RequestBody InstructorInsertDTO instructorInsertDTO,
            BindingResult bindingResult
    )   throws AppObjectAlreadyExistsException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        InstructorReadOnlyDTO instructorReadOnlyDTO = instructorService.saveInstructor(instructorInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()                       // request URI is /instructors
                .path("/{uuid}")                            // Appends "/{uuid}"
                .buildAndExpand(instructorReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(instructorReadOnlyDTO);
    }


    @Operation(
            summary = "Get all instructors",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Instructors returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InstructorReadOnlyDTO.class))),
                    @ApiResponse(
                            responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class)))
            }
    )
    @GetMapping("/instructors/getAllInstructors")
    public ResponseEntity<List<InstructorReadOnlyDTO>> getAllInstructors() {
        List<InstructorReadOnlyDTO> instructors = instructorService.getAllInstructors();
        return ResponseEntity.ok(instructors);
    }


    @Operation(
            summary = "Get instructors paginated and optionally filtered", // Use body {} or { \"page\": 0, \"pageSize\": 10 } for simple pagination; add lastname, uuid for filters.
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Instructors returned",
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
    @PostMapping("/instructors/search")
    public ResponseEntity<Paginated<InstructorReadOnlyDTO>> getFilteredAndPaginatedInstructors(
            @Nullable @RequestBody InstructorFilters filters)  {

        if (filters == null) filters = InstructorFilters.builder().build();
        Paginated<InstructorReadOnlyDTO> dtoPaginated = instructorService.getInstructorsFilteredPaginated(filters);
        return ResponseEntity.ok(dtoPaginated);
    }


    @Operation(
            summary = "Get one instructor by uuid",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Instructor returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InstructorReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Instructor not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401", description = "Not Authenticated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    )
            }
    )
    @GetMapping("/instructors/{uuid}")
    public ResponseEntity<InstructorReadOnlyDTO> getInstructorByUuid(@PathVariable String uuid)
            throws AppObjectNotFoundException {
        return ResponseEntity.ok(instructorService.getOneInstructor(uuid));
    }


    @Operation(
            summary = "Update an instructor",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Instructor updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InstructorReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Instructor already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Instructor not found",
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
                            responseCode = "401", description = "Not Authenticated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403", description = "Access Denied",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDTO.class))
                    )
            }
    )
    @PutMapping(value = "/instructors/{uuid}")
    @PreAuthorize("#uuid == #instructorUpdateDTO.uuid()")  // AccessDeniedException
    public ResponseEntity<InstructorReadOnlyDTO> updateInstructor(@PathVariable String uuid,
                                                            @Valid @RequestBody InstructorUpdateDTO instructorUpdateDTO,
                                                            BindingResult bindingResult)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        InstructorReadOnlyDTO instructorReadOnlyDTO = instructorService.updateInstructor(instructorUpdateDTO);

        return ResponseEntity.ok(instructorReadOnlyDTO);
    }
}
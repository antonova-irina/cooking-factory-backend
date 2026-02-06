package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.InstructorFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.dto.*;

import java.util.List;

public interface IInstructorService {
    InstructorReadOnlyDTO saveInstructor(InstructorInsertDTO instructorInsertDTO)
            throws AppObjectAlreadyExistsException;

    InstructorReadOnlyDTO updateInstructor(InstructorUpdateDTO instructorUpdateDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException;

    InstructorReadOnlyDTO getOneInstructor(String uuid) throws AppObjectNotFoundException;

    List<InstructorReadOnlyDTO> getAllInstructors();

    Paginated<InstructorReadOnlyDTO> getPaginatedInstructors(int page, int size);

    Paginated<InstructorReadOnlyDTO> getInstructorsFilteredPaginated(InstructorFilters instructorFilters);
}


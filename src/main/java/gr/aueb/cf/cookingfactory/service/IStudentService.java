package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.core.filters.StudentFilters;
import gr.aueb.cf.cookingfactory.dto.StudentInsertDTO;
import gr.aueb.cf.cookingfactory.dto.StudentReadOnlyDTO;
import gr.aueb.cf.cookingfactory.dto.StudentUpdateDTO;

import java.util.List;

public interface IStudentService {
    StudentReadOnlyDTO saveStudent(StudentInsertDTO studentInsertDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException;

    StudentReadOnlyDTO updateStudent(StudentUpdateDTO studentUpdateDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException;

    StudentReadOnlyDTO getOneStudent(String uuid) throws AppObjectNotFoundException;

    List<StudentReadOnlyDTO> getAllStudents();

    Paginated<StudentReadOnlyDTO> getPaginatedStudents(int page, int size);

    Paginated<StudentReadOnlyDTO> getStudentsFilteredPaginated(StudentFilters studentFilters);
}

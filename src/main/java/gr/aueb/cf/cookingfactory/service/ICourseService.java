package gr.aueb.cf.cookingfactory.service;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.CourseFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.dto.*;

import java.util.List;

public interface ICourseService {
    CourseReadOnlyDTO saveCourse(CourseInsertDTO courseInsertDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException;

    CourseReadOnlyDTO updateCourse(CourseUpdateDTO courseUpdateDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException;

    CourseReadOnlyDTO getOneCourse(Long id) throws AppObjectNotFoundException;

    List<CourseReadOnlyDTO> getAllCourses();

    Paginated<CourseReadOnlyDTO> getPaginatedCourses(int page, int size);

    Paginated<CourseReadOnlyDTO> getCoursesFilteredPaginated(CourseFilters courseFilters);
}


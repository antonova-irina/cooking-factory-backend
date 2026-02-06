package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.InstructorFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.core.specifications.InstructorSpecification;
import gr.aueb.cf.cookingfactory.dto.InstructorInsertDTO;
import gr.aueb.cf.cookingfactory.dto.InstructorReadOnlyDTO;
import gr.aueb.cf.cookingfactory.dto.InstructorUpdateDTO;
import gr.aueb.cf.cookingfactory.mapper.Mapper;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.repository.InstructorRepository;
import gr.aueb.cf.cookingfactory.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorService implements IInstructorService {
    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    //Save (add) a new instructor

    @Override
    @Transactional(rollbackOn = Exception.class)
    public InstructorReadOnlyDTO saveInstructor(InstructorInsertDTO instructorInsertDTO)
            throws AppObjectAlreadyExistsException {

        if (instructorRepository.findByIdentityNumber(instructorInsertDTO.identityNumber()).isPresent()) {
            throw new AppObjectAlreadyExistsException("IdentityNumber", "Instructor with identity number " + instructorInsertDTO.identityNumber() + " already exists");
        }

        if (userRepository.findByUsername(instructorInsertDTO.userInsertDTO().username()).isPresent()) {
            throw new AppObjectAlreadyExistsException("Username", "User with username " + instructorInsertDTO.userInsertDTO().username() + " already exists");
        }

        Instructor instructor = mapper.mapToInstructorEntity(instructorInsertDTO);

        Instructor savedInstructor = instructorRepository.save(instructor);    // Exception

        log.info("Instructor with identity number={} saved.", instructorInsertDTO.identityNumber());
        return mapper.mapToInstructorReadOnlyDTO(savedInstructor);
    }

    // Update an existing instructor

    @Override
    @Transactional(rollbackOn = Exception.class)
    public InstructorReadOnlyDTO updateInstructor(InstructorUpdateDTO instructorUpdateDTO)
            throws AppObjectAlreadyExistsException, AppObjectNotFoundException {

        Instructor existingInstructor = instructorRepository.findById(instructorUpdateDTO.id())
                .orElseThrow(() -> new AppObjectNotFoundException("Instructor", "Instructor with id " + instructorUpdateDTO.id() + " not found"));

        if (!existingInstructor.getIdentityNumber().equals(instructorUpdateDTO.identityNumber()) &&
                instructorRepository.findByIdentityNumber(instructorUpdateDTO.identityNumber()).isPresent()) {
            throw new AppObjectAlreadyExistsException("Instructor", "Instructor with identity number " + instructorUpdateDTO.identityNumber() + " already exists");
        }

        String newUsername = instructorUpdateDTO.userUpdateDTO().username();
        if (!existingInstructor.getUser().getUsername().equals(newUsername) &&
                userRepository.findByUsername(newUsername).isPresent()) {
            throw new AppObjectAlreadyExistsException("Username", "User with username " + newUsername + " already exists");
        }

        Instructor instructorToUpdate = mapper.mapToInstructorEntity(instructorUpdateDTO, existingInstructor.getUser().getPassword());

        Instructor updatedInstructor = instructorRepository.save(instructorToUpdate);

        log.info("Instructor with id={} updated.", instructorUpdateDTO.id());
        return mapper.mapToInstructorReadOnlyDTO(updatedInstructor);
    }

    // Get an instructor by uuid

    @Override
    @Transactional
    public InstructorReadOnlyDTO getOneInstructor(String uuid) throws AppObjectNotFoundException {
        return instructorRepository
                .findByUuid(uuid)
                .map(mapper::mapToInstructorReadOnlyDTO)
                .orElseThrow(() ->
                        new AppObjectNotFoundException("Instructor", "Instructor with uuid:" + uuid + " not found"));
    }

    // Get all instructors listed

    @Override
    @Transactional
    public List<InstructorReadOnlyDTO> getAllInstructors() {
        List<Instructor> instructors = instructorRepository.findAll();
        return instructors.stream()
                .map(mapper::mapToInstructorReadOnlyDTO)
                .collect(Collectors.toList());
    }

    // Get all instructors paginated, sorted by id

    @Override
    @Transactional
    public Paginated<InstructorReadOnlyDTO> getPaginatedInstructors(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());
        log.debug("Paginated instructors were returned successfully with page={} and size={}", page, size);
        var paginatedInstructors = instructorRepository.findAll(pageable);
        return Paginated.fromPage(paginatedInstructors.map(mapper::mapToInstructorReadOnlyDTO));
    }

    @Override
    @Transactional
    public Paginated<InstructorReadOnlyDTO> getInstructorsFilteredPaginated(InstructorFilters instructorFilters) {
        var filtered = instructorRepository.findAll(getSpecsFromFilters(instructorFilters), instructorFilters.getPageable());
        log.debug("Filtered and paginated instructors were returned successfully with page={} and size={}", instructorFilters.getPage(),
                instructorFilters.getPageSize());
        return Paginated.fromPage(filtered.map(mapper::mapToInstructorReadOnlyDTO));
    }

    private Specification<Instructor> getSpecsFromFilters(InstructorFilters instructorFilters) {
        return InstructorSpecification.instructorStringLastnameLike(instructorFilters.getLastname())
                .and(InstructorSpecification.instructorUuidIs(instructorFilters.getUuid()));
    }
}





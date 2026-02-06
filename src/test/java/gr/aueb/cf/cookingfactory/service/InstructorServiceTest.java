package gr.aueb.cf.cookingfactory.service;

import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.cookingfactory.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.cookingfactory.core.filters.InstructorFilters;
import gr.aueb.cf.cookingfactory.core.filters.Paginated;
import gr.aueb.cf.cookingfactory.dto.*;
import gr.aueb.cf.cookingfactory.mapper.Mapper;
import gr.aueb.cf.cookingfactory.model.Instructor;
import gr.aueb.cf.cookingfactory.model.User;
import gr.aueb.cf.cookingfactory.repository.InstructorRepository;
import gr.aueb.cf.cookingfactory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private InstructorService instructorService;

    private Instructor instructor;
    private User user;
    private InstructorReadOnlyDTO instructorReadOnlyDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("dimitris.chef");
        user.setPassword("hashedPassword");
        user.setRole(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR);
        user.setVat("123456789");

        instructor = new Instructor();
        instructor.setId(1L);
        instructor.setUuid("uuid-123");
        instructor.setIsActive(true);
        instructor.setFirstname("Dimitris");
        instructor.setLastname("Papadopoulos");
        instructor.setIdentityNumber("ID123");
        instructor.setUser(user);

        instructorReadOnlyDTO = InstructorReadOnlyDTO.builder()
                .id(1L)
                .uuid("uuid-123")
                .isActive(true)
                .firstname("Dimitris")
                .lastname("Papadopoulos")
                .identityNumber("ID123")
                .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                .userReadOnlyDTO(new UserReadOnlyDTO(1L, "dimitris.chef", gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR, "123456789"))
                .contactDetailsReadOnlyDTO(ContactDetailsReadOnlyDTO.builder().id(1L).city("Athens").email("a@b.gr").phoneNumber("123").build())
                .build();
    }

    @Nested
    @DisplayName("saveInstructor")
    class SaveInstructor {

        @Test
        @DisplayName("saves instructor when identity number and username are unique")
        void saveInstructor_success() throws Exception {
            UserInsertDTO userDTO = UserInsertDTO.builder()
                    .isActive(true)
                    .username("maria.chef")
                    .password("Pass@123")
                    .role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR)
                    .vat("987654321")
                    .build();
            InstructorInsertDTO insertDTO = InstructorInsertDTO.builder()
                    .isActive(true)
                    .firstname("Maria")
                    .lastname("Konstantinou")
                    .identityNumber("ID456")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .userInsertDTO(userDTO)
                    .contactDetailsInsertDTO(ContactDetailsInsertDTO.builder().city("Athens").email("j@b.gr").phoneNumber("456").build())
                    .build();

            when(instructorRepository.findByIdentityNumber("ID456")).thenReturn(Optional.empty());
            when(userRepository.findByUsername("maria.chef")).thenReturn(Optional.empty());
            when(mapper.mapToInstructorEntity(insertDTO)).thenReturn(new Instructor());
            when(instructorRepository.save(any(Instructor.class))).thenAnswer(inv -> {
                Instructor i = inv.getArgument(0);
                i.setId(2L);
                return i;
            });
            when(mapper.mapToInstructorReadOnlyDTO(any(Instructor.class))).thenReturn(
                    InstructorReadOnlyDTO.builder().id(2L).firstname("Maria").lastname("Konstantinou").identityNumber("ID456").gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE).build());

            InstructorReadOnlyDTO result = instructorService.saveInstructor(insertDTO);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(2L);
            verify(instructorRepository).findByIdentityNumber("ID456");
            verify(userRepository).findByUsername("maria.chef");
            verify(instructorRepository).save(any(Instructor.class));
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when identity number already exists")
        void saveInstructor_duplicateIdentityNumber_throwsException() {
            InstructorInsertDTO insertDTO = InstructorInsertDTO.builder()
                    .isActive(true)
                    .firstname("Eleni")
                    .lastname("Kyriakidou")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .userInsertDTO(UserInsertDTO.builder().isActive(true).username("x").password("Pass@1").role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR).vat("111111111").build())
                    .contactDetailsInsertDTO(ContactDetailsInsertDTO.builder().city("A").email("a@b.gr").phoneNumber("1").build())
                    .build();

            when(instructorRepository.findByIdentityNumber("ID123")).thenReturn(Optional.of(instructor));

            assertThatThrownBy(() -> instructorService.saveInstructor(insertDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("identity number")
                    .hasMessageContaining("ID123");
            verify(instructorRepository).findByIdentityNumber("ID123");
            verify(instructorRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when username already exists")
        void saveInstructor_duplicateUsername_throwsException() {
            UserInsertDTO userDTO = UserInsertDTO.builder()
                    .isActive(true)
                    .username("dimitris.chef")
                    .password("Pass@123")
                    .role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR)
                    .vat("987654321")
                    .build();
            InstructorInsertDTO insertDTO = InstructorInsertDTO.builder()
                    .isActive(true)
                    .firstname("Antonia")
                    .lastname("Nikolaou")
                    .identityNumber("ID456")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.FEMALE)
                    .userInsertDTO(userDTO)
                    .contactDetailsInsertDTO(ContactDetailsInsertDTO.builder().city("A").email("a@b.gr").phoneNumber("1").build())
                    .build();

            when(instructorRepository.findByIdentityNumber("ID456")).thenReturn(Optional.empty());
            when(userRepository.findByUsername("dimitris.chef")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> instructorService.saveInstructor(insertDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("dimitris.chef");
            verify(userRepository).findByUsername("dimitris.chef");
            verify(instructorRepository, org.mockito.Mockito.never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateInstructor")
    class UpdateInstructor {

        @Test
        @DisplayName("updates instructor successfully")
        void updateInstructor_success() throws Exception {
            UserUpdateDTO userUpdate = UserUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .username("dimitris.chef")
                    .role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();
            InstructorUpdateDTO updateDTO = InstructorUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(false)
                    .firstname("Dimitris")
                    .lastname("Nikolaou")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .userUpdateDTO(userUpdate)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("Thessaloniki").email("b@c.gr").phoneNumber("456").build())
                    .build();

            when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
            when(mapper.mapToInstructorEntity(eq(updateDTO), eq("hashedPassword"))).thenReturn(new Instructor());
            when(instructorRepository.save(any(Instructor.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.mapToInstructorReadOnlyDTO(any(Instructor.class))).thenReturn(instructorReadOnlyDTO);

            InstructorReadOnlyDTO result = instructorService.updateInstructor(updateDTO);

            assertThat(result).isNotNull();
            verify(instructorRepository).findById(1L);
            verify(instructorRepository).save(any(Instructor.class));
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when instructor does not exist")
        void updateInstructor_notFound_throwsException() {
            InstructorUpdateDTO updateDTO = InstructorUpdateDTO.builder()
                    .id(999L)
                    .uuid("uuid-999")
                    .isActive(true)
                    .firstname("Giorgos")
                    .lastname("Dimitriou")
                    .identityNumber("ID999")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .userUpdateDTO(UserUpdateDTO.builder().id(1L).isActive(true).username("x").role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR).vat("111111111").build())
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .build();

            when(instructorRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> instructorService.updateInstructor(updateDTO))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Instructor")
                    .hasMessageContaining("999");
            verify(instructorRepository).findById(999L);
            verify(instructorRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when new identity number is taken")
        void updateInstructor_duplicateIdentityNumber_throwsException() {
            Instructor other = new Instructor();
            other.setId(2L);
            other.setIdentityNumber("ID999");
            UserUpdateDTO userUpdate = UserUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .username("dimitris.chef")
                    .role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();
            InstructorUpdateDTO updateDTO = InstructorUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Dimitris")
                    .lastname("Papadopoulos")
                    .identityNumber("ID999")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .userUpdateDTO(userUpdate)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .build();

            when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
            when(instructorRepository.findByIdentityNumber("ID999")).thenReturn(Optional.of(other));

            assertThatThrownBy(() -> instructorService.updateInstructor(updateDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("identity number")
                    .hasMessageContaining("ID999");
            verify(instructorRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("throws AppObjectAlreadyExistsException when new username is taken")
        void updateInstructor_duplicateUsername_throwsException() {
            User otherUser = new User();
            otherUser.setId(2L);
            otherUser.setUsername("maria.chef");
            UserUpdateDTO userUpdate = UserUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .username("maria.chef")
                    .role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();
            InstructorUpdateDTO updateDTO = InstructorUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Dimitris")
                    .lastname("Papadopoulos")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .userUpdateDTO(userUpdate)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .build();

            when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
            when(userRepository.findByUsername("maria.chef")).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> instructorService.updateInstructor(updateDTO))
                    .isInstanceOf(AppObjectAlreadyExistsException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("maria.chef");
            verify(instructorRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("allows same identity number and username when updating same instructor")
        void updateInstructor_sameIdentityAndUsername_success() throws Exception {
            UserUpdateDTO userUpdate = UserUpdateDTO.builder()
                    .id(1L)
                    .isActive(true)
                    .username("dimitris.chef")
                    .role(gr.aueb.cf.cookingfactory.core.enums.Role.INSTRUCTOR)
                    .vat("123456789")
                    .build();
            InstructorUpdateDTO updateDTO = InstructorUpdateDTO.builder()
                    .id(1L)
                    .uuid("uuid-123")
                    .isActive(true)
                    .firstname("Dimitris")
                    .lastname("Papadopoulos")
                    .identityNumber("ID123")
                    .gender(gr.aueb.cf.cookingfactory.core.enums.Gender.MALE)
                    .userUpdateDTO(userUpdate)
                    .contactDetailsUpdateDTO(ContactDetailsUpdateDTO.builder().id(1L).city("A").email("a@b.gr").phoneNumber("1").build())
                    .build();

            when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
            when(mapper.mapToInstructorEntity(eq(updateDTO), eq("hashedPassword"))).thenReturn(new Instructor());
            when(instructorRepository.save(any(Instructor.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.mapToInstructorReadOnlyDTO(any(Instructor.class))).thenReturn(instructorReadOnlyDTO);

            InstructorReadOnlyDTO result = instructorService.updateInstructor(updateDTO);

            assertThat(result).isNotNull();
            verify(instructorRepository).findById(1L);
            verify(instructorRepository).save(any(Instructor.class));
        }
    }

    @Nested
    @DisplayName("getOneInstructor")
    class GetOneInstructor {

        @Test
        @DisplayName("returns instructor when uuid exists")
        void getOneInstructor_success() throws Exception {
            when(instructorRepository.findByUuid("uuid-123")).thenReturn(Optional.of(instructor));
            when(mapper.mapToInstructorReadOnlyDTO(instructor)).thenReturn(instructorReadOnlyDTO);

            InstructorReadOnlyDTO result = instructorService.getOneInstructor("uuid-123");

            assertThat(result).isNotNull();
            assertThat(result.uuid()).isEqualTo("uuid-123");
            assertThat(result.firstname()).isEqualTo("Dimitris");
            verify(instructorRepository).findByUuid("uuid-123");
        }

        @Test
        @DisplayName("throws AppObjectNotFoundException when uuid does not exist")
        void getOneInstructor_notFound_throwsException() {
            when(instructorRepository.findByUuid("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> instructorService.getOneInstructor("unknown"))
                    .isInstanceOf(AppObjectNotFoundException.class)
                    .hasMessageContaining("Instructor")
                    .hasMessageContaining("unknown");
            verify(instructorRepository).findByUuid("unknown");
        }
    }

    @Nested
    @DisplayName("getAllInstructors")
    class GetAllInstructors {

        @Test
        @DisplayName("returns empty list when no instructors")
        void getAllInstructors_emptyList() {
            when(instructorRepository.findAll()).thenReturn(List.of());

            List<InstructorReadOnlyDTO> result = instructorService.getAllInstructors();

            assertThat(result).isEmpty();
            verify(instructorRepository).findAll();
        }

        @Test
        @DisplayName("returns all instructors")
        void getAllInstructors_returnsAll() {
            when(instructorRepository.findAll()).thenReturn(List.of(instructor));
            when(mapper.mapToInstructorReadOnlyDTO(instructor)).thenReturn(instructorReadOnlyDTO);

            List<InstructorReadOnlyDTO> result = instructorService.getAllInstructors();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).firstname()).isEqualTo("Dimitris");
            verify(instructorRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getPaginatedInstructors")
    class GetPaginatedInstructors {

        @Test
        @DisplayName("returns paginated instructors")
        void getPaginatedInstructors_success() {
            Pageable pageable = PageRequest.of(0, 10);
            when(instructorRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(instructor), pageable, 1));
            when(mapper.mapToInstructorReadOnlyDTO(instructor)).thenReturn(instructorReadOnlyDTO);

            Paginated<InstructorReadOnlyDTO> result = instructorService.getPaginatedInstructors(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getData()).hasSize(1);
            assertThat(result.getCurrentPage()).isZero();
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(instructorRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("returns empty page when no instructors")
        void getPaginatedInstructors_emptyPage() {
            Pageable pageable = PageRequest.of(1, 5);
            when(instructorRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Paginated<InstructorReadOnlyDTO> result = instructorService.getPaginatedInstructors(1, 5);

            assertThat(result.getData()).isEmpty();
            assertThat(result.getCurrentPage()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("getInstructorsFilteredPaginated")
    class GetInstructorsFilteredPaginated {

        @Test
        @DisplayName("returns filtered and paginated instructors")
        void getInstructorsFilteredPaginated_success() {
            InstructorFilters filters = InstructorFilters.builder()
                    .lastname("Papadopoulos")
                    .build();
            filters.setPage(0);
            filters.setPageSize(10);

            when(instructorRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(instructor)));
            when(mapper.mapToInstructorReadOnlyDTO(instructor)).thenReturn(instructorReadOnlyDTO);

            Paginated<InstructorReadOnlyDTO> result = instructorService.getInstructorsFilteredPaginated(filters);

            assertThat(result).isNotNull();
            assertThat(result.getData()).hasSize(1);
            verify(instructorRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}

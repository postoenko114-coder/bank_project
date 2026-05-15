package com.example.demo.unit.service;

import com.example.demo.dto.user.UserCreateDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOAdmin;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UserMapperImpl;
import com.example.demo.models.user.RoleUser;
import com.example.demo.models.user.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Test
    void createUser_ShouldCreateUser_WhenDataIsCorrect() {
        UserCreateDTO dto = new UserCreateDTO("alex", "test@gmail.com", "12345", "CLIENT");

        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed_12345");

        UserDTOAdmin result = userServiceImpl.createUser(dto);

        assertNotNull(result);
        assertEquals("alex", result.getUsername());
        assertEquals("test@gmail.com", result.getEmail());

        verify(passwordEncoder, times(1)).encode("12345");
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void createUser_ShouldThrowConflict_WhenUsernameExists() {
        UserCreateDTO dto = new UserCreateDTO("alex", "test@gmail.com", "12345", "CLIENT");

        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userServiceImpl.createUser(dto)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username already exists", exception.getReason());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowConflict_WhenEmailExists() {
        UserCreateDTO dto = new UserCreateDTO("alex", "test@gmail.com", "12345", "CLIENT");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userServiceImpl.createUser(dto)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email already in use", exception.getReason());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");
        when (userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

        User result = userServiceImpl.getUserById(1L);

        assertNotNull(result);
        assertEquals("alex", result.getRealUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userServiceImpl.getUserById(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsersAndMapToDTO() {
        User user1 = new User();
        user1.setUsername("alex");

        User user2 = new User();
        user2.setUsername("maria");

        List<User> fakeList = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(fakeList);

        List<UserDTOAdmin> result = userServiceImpl.getAllUsers();

        assertEquals(2, result.size());

        assertEquals("alex", result.get(0).getUsername());
        assertEquals("maria", result.get(1).getUsername());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void searchUsers_ShouldReturnListUserDTOs_WhenQueryIsNotEmpty() {
        User user = new User();
        user.setUsername("alex");

        List<User> fakeList = List.of(user);

        String name =  "alex";

        when(userRepository.findByEmailOrUsernameOrId(name)).thenReturn(fakeList);

        List<UserDTOAdmin> result = userServiceImpl.searchUsers(name);

        assertEquals(1, result.size());
        assertEquals("alex", result.get(0).getUsername());
        verify(userRepository, times(1)).findByEmailOrUsernameOrId(name);
    }

    @Test
    void searchUsers_ShouldReturnAllUserDTOs_WhenQueryIsNullOrBlank() {
        User user1 = new User(); user1.setUsername("alex");
        User user2 = new User(); user2.setUsername("maria");
        List<User> fakeList = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(fakeList);

        List<UserDTOAdmin> result = userServiceImpl.searchUsers("   ");

        assertEquals(2, result.size());

        verify(userRepository, times(1)).findAll();
        verify(userRepository, never()).findByEmailOrUsernameOrId(anyString());
    }

    @Test
    void findUserByUsername_ShouldReturnUser_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setUsername("alex");

        when(userRepository.findByUsername(fakeUser.getUsername())).thenReturn(Optional.of(fakeUser));

        User result = userServiceImpl.findUserByUsername(fakeUser.getUsername());

        assertNotNull(result);
        assertEquals("alex", result.getRealUsername());
        verify(userRepository, times(1)).findByUsername(fakeUser.getUsername());
    }

    @Test
    void findUserByUsername_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("alex")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userServiceImpl.findUserByUsername("alex")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findByUsername("alex");
    }

    @Test
    void findUserByEmail_ShouldReturnUser_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setEmail("test@gmail.com");

        when(userRepository.findByEmail(fakeUser.getEmail())).thenReturn(Optional.of(fakeUser));

        User result = userServiceImpl.findUserByEmail(fakeUser.getEmail());

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail(fakeUser.getEmail());
    }

    @Test
    void findUserByEmail_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userServiceImpl.findUserByEmail("test@gmail.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findByEmail("test@gmail.com");
    }

    @Test
    void checkEmail_ShouldReturnTrue_WhenEmailExists() {
        String email = "test@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        Boolean exists = userServiceImpl.checkEmail(email);

        assertTrue(exists);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void checkEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        String email = "new@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Boolean exists = userServiceImpl.checkEmail(email);

        assertFalse(exists);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void updateUser_ShouldReturnUserDTO_WhenUserExistsAndUsernameAndEmailChanged(){
        UserDTO fakeUserDTO =  new UserDTO();
        fakeUserDTO.setUsername("alex");
        fakeUserDTO.setEmail("fake@mail.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("oleg");
        user.setEmail("test@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(userRepository.findByEmail(fakeUserDTO.getEmail())).thenReturn(Optional.empty());

        UserDTO result =  userServiceImpl.updateUser(user.getId(), fakeUserDTO);

        assertEquals(fakeUserDTO.getUsername(), result.getUsername());
        assertEquals(fakeUserDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).findByEmail(fakeUserDTO.getEmail());
    }

    @Test
    void updateUser_ShouldReturnUserDTO_WhenUserExistsAndUsernameChanged(){
        UserDTO fakeUserDTO =  new UserDTO();
        fakeUserDTO.setUsername("alex");
        fakeUserDTO.setEmail("test@mail.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("oleg");
        user.setEmail("test@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDTO result =  userServiceImpl.updateUser(user.getId(), fakeUserDTO);

        assertEquals(fakeUserDTO.getUsername(), result.getUsername());
        assertEquals(fakeUserDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void updateUser_ShouldReturnUserDTO_WhenUserExistsAndEmailChanged(){
        UserDTO fakeUserDTO =  new UserDTO();
        fakeUserDTO.setUsername("alex");
        fakeUserDTO.setEmail("fake@mail.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("alex");
        user.setEmail("test@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(userRepository.findByEmail(fakeUserDTO.getEmail())).thenReturn(Optional.empty());

        UserDTO result =  userServiceImpl.updateUser(user.getId(), fakeUserDTO);

        assertEquals(fakeUserDTO.getUsername(), result.getUsername());
        assertEquals(fakeUserDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).findByEmail(fakeUserDTO.getEmail());
    }

    @Test
    void updateUser_ShouldReturnUserDTO_WhenUserExistsAndNothingChanged(){
        UserDTO fakeUserDTO =  new UserDTO();
        fakeUserDTO.setUsername("alex");
        fakeUserDTO.setEmail("test@mail.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("alex");
        user.setEmail("test@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDTO result =  userServiceImpl.updateUser(user.getId(), fakeUserDTO);

        assertEquals(fakeUserDTO.getUsername(), result.getUsername());
        assertEquals(fakeUserDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void updateUser_ShouldThrowNotFound_WhenUserDoesNotExist(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userServiceImpl.updateUser(1L, new UserDTO()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateUser_ShouldThrowConflict_WhenNewEmailIsTaken() {
        UserDTO fakeUserDTO = new UserDTO();
        fakeUserDTO.setUsername("oleg");
        fakeUserDTO.setEmail("admin@mail.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("oleg");
        user.setEmail("test@mail.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(userRepository.findByEmail(fakeUserDTO.getEmail())).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userServiceImpl.updateUser(user.getId(), fakeUserDTO));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email is already taken", exception.getReason());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).findByEmail(fakeUserDTO.getEmail());
    }

    @Test
    void changeRole_ShouldReturnUserDTOAdmin_WhenUserExists(){
        User user = new User();
        user.setId(1L);
        user.setRoleUser(RoleUser.CLIENT);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDTOAdmin result = userServiceImpl.changeRole(1L, "admin");

        assertNotNull(result);
        assertEquals(RoleUser.ADMIN, result.getRoleUser());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void changeRole_ShouldThrowNotFound_WhenUserDoesNotExist(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception  =assertThrows(ResponseStatusException.class,
                () -> userServiceImpl.changeRole(1L, "admin"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void changePassword_ShouldReturnUserDTO_WhenUserExistsAndOldPasswordMatches(){
        String rawOldPassword = "oldPassword";
        String dbHashedOldPassword = "db_hashed_old_password"; // То, что лежало в базе
        String rawNewPassword = "newPassword";
        String hashedNewPassword = "hashed_new_password";      // То, что вернет энкодер

        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");
        fakeUser.setPassword(dbHashedOldPassword); // Кладем старый хеш

        when(userRepository.findById(fakeUser.getId())).thenReturn(Optional.of(fakeUser));
        when(passwordEncoder.matches(rawOldPassword, dbHashedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(rawNewPassword)).thenReturn(hashedNewPassword);

        UserDTO result = userServiceImpl.changePassword(fakeUser.getId(), rawOldPassword, rawNewPassword);

        assertNotNull(result);
        assertEquals(hashedNewPassword, result.getPassword());

        verify(userRepository, times(1)).findById(fakeUser.getId());
        verify(passwordEncoder, times(1)).matches(rawOldPassword, dbHashedOldPassword);
        verify(passwordEncoder, times(1)).encode(rawNewPassword);
    }

    @Test
    void changePassword_ShouldReturnUserDTO_WhenUserExistsAndOldPasswordDoesntMatches(){
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");
        fakeUser.setPassword("oldPassword");

        String oldPassword = "PasswordDoesntMatch";
        String newPassword = "newPassword";

        when(userRepository.findById(fakeUser.getId())).thenReturn(Optional.of(fakeUser));
        when(passwordEncoder.matches(oldPassword,fakeUser.getPassword())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () ->  userServiceImpl.changePassword(fakeUser.getId(), oldPassword, newPassword));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Old Password not match", exception.getReason());
        verify(passwordEncoder, times(1)).matches(oldPassword,fakeUser.getPassword());
        verify(userRepository, times(1)).findById(fakeUser.getId());
    }

    @Test
    void changePassword_ShouldThrowNotFound_WhenUserDoesNotExist(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userServiceImpl.changePassword(1L, "oldPassword", "newPassword"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void deleteUserById_ShouldCallRepositoryDelete() {
        Long userId = 1L;

        userServiceImpl.deleteUserById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

}

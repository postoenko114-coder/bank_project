package com.alex.bank.services.user;

import com.alex.bank.dto.user.UserCreateDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.dto.user.UserDTOAdmin;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    @Transactional
    UserDTOAdmin createUser(UserCreateDTO createDTO);

    @Transactional
    UserDTO getUserById(Long user_id);

    @Transactional
    List<UserDTOAdmin> getAllUsers();

    @Transactional
    List<UserDTOAdmin> searchUsers(String query);

    @Transactional
    UserDTO findUserByUsername(String userName);

    @Transactional
    UserDTO findUserByEmail(String email);

    @Transactional
    Boolean checkEmail(String email);

    @Transactional
    UserDTO updateUser(Long user_id, UserDTO userDTO);

    @Transactional
    UserDTOAdmin changeRole(Long user_id, String role);

    @Transactional
    UserDTO changePassword(Long user_id, String oldPassword, String newPassword);

    @Transactional
    void deleteUserById(Long user_id);


}

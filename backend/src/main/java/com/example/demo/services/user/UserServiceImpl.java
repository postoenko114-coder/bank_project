package com.example.demo.services.user;

import com.example.demo.dto.user.UserCreateDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOAdmin;
import com.example.demo.mapper.UserMapper;
import com.example.demo.models.user.RoleUser;
import com.example.demo.models.user.User;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDTOAdmin createUser(UserCreateDTO createDTO) {
        if (userRepository.findByUsername(createDTO.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.findByEmail(createDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = userMapper.toEntity(createDTO);

        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        userRepository.save(user);
        return userMapper.toDTOAdmin(user);
    }


    @Transactional
    @Override
    public User getUserById(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return user;
    }

    @Transactional
    @Override
    public List<UserDTOAdmin> getAllUsers(){
        List<User> users = userRepository.findAll();
        List<UserDTOAdmin> userDTOAdmins = new ArrayList<>();
        for(User user : users){
            userDTOAdmins.add(userMapper.toDTOAdmin(user));
        }
        return userDTOAdmins;
    }

    @Transactional
    @Override
    public List<UserDTOAdmin> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        List<User> users = userRepository.findByEmailOrUsernameOrId(query);
        List<UserDTOAdmin> userDTOAdmins = new ArrayList<>();
        for(User user : users){
            userDTOAdmins.add(userMapper.toDTOAdmin(user));
        }
        return userDTOAdmins;
    }

    @Transactional
    @Override
    public User findUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
    }

    @Transactional
    @Override
    public User findUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
    }

    @Transactional
    @Override
    public Boolean checkEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    @Override
    public UserDTO updateUser(Long user_id, UserDTO userDTO){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
        }
        if(!userDTO.getUsername().equals(user.getUsername())){
            user.setUsername(userDTO.getUsername());
        }
        if(!userDTO.getEmail().equals(user.getEmail())){
            user.setEmail(userDTO.getEmail());
        }
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public UserDTOAdmin changeRole(Long user_id, String role){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        user.setRoleUser(RoleUser.valueOf(role.toUpperCase()));
        return userMapper.toDTOAdmin(user);
    }

    @Transactional
    @Override
    public UserDTO changePassword(Long user_id, String oldPassword, String newPassword){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        if(!passwordEncoder.matches(oldPassword,user.getPassword())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Old Password not match");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.toDTO(user);
    }

    @Transactional
    @Override
    public void deleteUserById(Long user_id){
       userRepository.deleteById(user_id);
    }

}

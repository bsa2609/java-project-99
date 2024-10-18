package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.EntityNotUniqueException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO get(long id) {
        User user = findById(id);

        return userMapper.map(user);
    }

    public UserDTO create(UserCreateDTO data) {
        String email = data.getEmail();
        if (isEmailExists(email)) {
            throw new EntityNotUniqueException(
                    String.format("User with email %s already exists", email)
            );
        }

        User user = userMapper.map(data);
        userRepository.save(user);

        return userMapper.map(user);
    }

    public UserDTO update(long id, UserUpdateDTO data) {
        User user = findById(id);

        userMapper.update(data, user);
        userRepository.save(user);

        return userMapper.map(user);
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id %s not found", id)
                ));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with email %s not found", email)
                ));
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}

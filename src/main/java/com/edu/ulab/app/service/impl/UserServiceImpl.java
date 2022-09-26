package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.InvalidRequestException;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.repository.UserRepository;
import com.edu.ulab.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        Person user = userMapper.userDtoToPerson(userDto);
        log.info("Mapped user: {}", user);
        Person savedUser = userRepository.save(user);
        log.info("Saved user: {}", savedUser);
        return userMapper.personToUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {


        Person person = userRepository.findPersonByFullName(userDto.getFullName());

        if (person == null) throw new NotFoundException("Person not found");
        Long id = person.getId();

        person.setAge(userDto.getAge());
        person.setFullName(userDto.getFullName());
        person.setTitle(userDto.getTitle());
        //person.setId(id);
        userRepository.save(person);
        return userMapper.personToUserDto(person);

    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Get user by id: {}", id);
        Optional<Person> person = userRepository.findById(id);

        if (!person.isPresent()) throw new NotFoundException("Person not found");

        UserDto userDto = userMapper.personToUserDto(person.get());
        return userDto;
    }

    @Override
    public void deleteUserById(Long id) {
        log.info("Get user by id: {}", id);
        Optional<Person> person = userRepository.findById(id);

        if (!person.isPresent()) throw new NotFoundException("Person not found");

        log.info("Delete user by id: {}", id);
        userRepository.delete(person.get());
    }
}

package com.edu.ulab.app.facade;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.exception.InvalidRequestException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.mapper.UserMapper;

import com.edu.ulab.app.service.impl.BookServiceImpl;
import com.edu.ulab.app.service.impl.BookServiceImplTemplate;
import com.edu.ulab.app.service.impl.UserServiceImpl;
import com.edu.ulab.app.service.impl.UserServiceImplTemplate;
import com.edu.ulab.app.web.request.UserBookRequest;
import com.edu.ulab.app.web.response.UserBookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserDataFacade {
    private final UserServiceImplTemplate userService;
    private final BookServiceImplTemplate bookService;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    public UserDataFacade(UserServiceImplTemplate userService,
                          BookServiceImplTemplate bookService,
                          UserMapper userMapper,
                          BookMapper bookMapper) {
        this.userService = userService;
        this.bookService = bookService;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
    }

    public UserBookResponse createUserWithBooks(UserBookRequest userBookRequest) {
        log.info("Got user book create request: {}", userBookRequest);
        UserDto userDto = userMapper.userRequestToUserDto(userBookRequest.getUserRequest());
        log.info("Mapped user request: {}", userDto);

        UserDto createdUser = userService.createUser(userDto);
        log.info("Created user: {}", createdUser);

        List<Long> bookIdList = userBookRequest.getBookRequests()
                .stream()
                .filter(Objects::nonNull)
                .map(bookMapper::bookRequestToBookDto)
                .peek(bookDto -> bookDto.setUserId(createdUser.getId()))
                .peek(mappedBookDto -> log.info("mapped book: {}", mappedBookDto))
                .map(bookService::createBook)
                .peek(createdBook -> log.info("Created book: {}", createdBook))
                .map(BookDto::getId)
                .toList();
        log.info("Collected book ids: {}", bookIdList);

        return UserBookResponse.builder()
                .userId(createdUser.getId())
                .booksIdList(bookIdList)
                .build();
    }

    public UserBookResponse updateUserWithBooks(UserBookRequest userBookRequest) {
        if (userBookRequest.getUserRequest() == null) throw new InvalidRequestException("Invalid Request Exception");

        UserDto userDto = userMapper.userRequestToUserDto(userBookRequest.getUserRequest());
        log.info("Mapped user request: {}", userDto);

        UserDto updatedUser=userService.updateUser(userDto);
        List<Long> bookIdList = new ArrayList<>();

        if(updatedUser==null){ createUserWithBooks(userBookRequest);}
        else {
             log.info("Update user: {}", updatedUser);

            bookIdList = userBookRequest.getBookRequests()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(bookMapper::bookRequestToBookDto)
                    .peek(bookDto -> bookDto.setUserId(updatedUser.getId()))
                    .peek(mappedBookDto -> log.info("mapped book: {}", mappedBookDto))
                    .map(bookService::updateBook)
                    .peek(updatedBook -> log.info("Update book: {}", updatedBook))
                    .map(BookDto::getId)
                    .toList();
            log.info("Collected book ids: {}", bookIdList);
        }
        return UserBookResponse.builder()
                .userId(updatedUser.getId())
                .booksIdList(bookIdList)
                .build();

    }

    public UserBookResponse getUserWithBooks(Long userId) {
        log.info("Got user with book get request: {}", userId);

        UserDto userDto = userService.getUserById(userId);
        log.info("Mapped userDto response: {}", userDto);

        log.info("Got list books");
        List<Long> bookDtoList = bookService.getBookById(userId)
                .stream()
                .peek(bookDto -> log.info("Mapped bookDto response: {}", bookDto))
                .map(BookDto::getId)
                .collect(Collectors.toList());

        return UserBookResponse.builder()
                        .userId(userDto.getId())
                        .booksIdList(bookDtoList)
                        .build();

    }

    public void deleteUserWithBooks(Long userId) {
        log.info("Delete book by id: {}", userId);
        bookService.deleteBookById(userId);
        log.info("Delete user by id:{}",userId);
        userService.deleteUserById(userId);
    }
}

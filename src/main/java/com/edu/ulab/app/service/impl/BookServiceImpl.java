package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.InvalidRequestException;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.repository.BookRepository;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        Book book = bookMapper.bookDtoToBook(bookDto);
        log.info("Mapped book: {}", book);
        Book savedBook = bookRepository.save(book);
        log.info("Saved book: {}", savedBook);
        return bookMapper.bookToBookDto(savedBook);
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        log.info("Get book");
        Book book = bookRepository.findBookByTitleAndUserId(bookDto.getTitle(), bookDto.getUserId());
        if (book == null) {
            book = bookMapper.bookDtoToBook(createBook(bookDto));
        } else {
            book.setAuthor(bookDto.getAuthor());
            book.setPageCount(bookDto.getPageCount());
            book.setTitle(bookDto.getTitle());
            book.setUserId(bookDto.getUserId());
        }
        log.info("Save book: {}", book);
        bookRepository.save(book);
        return bookMapper.bookToBookDto(book);

    }

    @Override
    public List<BookDto> getBookById(Long userId) {
        log.info("Get book by User id: {}", userId);
        List<Book> listBook = bookRepository.findBookByUserId(userId);
        List<BookDto> listBookDto = listBook
                .stream()
                .map(book -> bookMapper.bookToBookDto(book))
                .toList();
        return listBookDto;
    }

    @Override
    public void deleteBookById(Long id) {
        log.info("Get book by id: {}", id);
        Optional<Book> book = bookRepository.findById(id);

        if (!book.isPresent()) throw new NotFoundException("Person not found");

        log.info("Delete book by id: {}", id);
        bookRepository.delete(book.get());
    }
}

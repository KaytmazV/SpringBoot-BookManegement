package com.volkankaytmaz.bookmanagement.service;

import com.volkankaytmaz.bookmanagement.dto.BookDTO;
import com.volkankaytmaz.bookmanagement.exception.ResourceNotFoundException;
import com.volkankaytmaz.bookmanagement.model.Author;
import com.volkankaytmaz.bookmanagement.model.Book;
import com.volkankaytmaz.bookmanagement.model.Category;
import com.volkankaytmaz.bookmanagement.repository.AuthorRepository;
import com.volkankaytmaz.bookmanagement.repository.BookRepository;
import com.volkankaytmaz.bookmanagement.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,
                       CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return modelMapper.map(book, BookDTO.class);
    }

    @Transactional
    public BookDTO createBook(BookDTO bookDTO) {
        Book book = modelMapper.map(bookDTO, Book.class);
        if (bookDTO.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDTO.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + bookDTO.getAuthorId()));
            book.setAuthor(author);
        }

        if (bookDTO.getCategoryIds() != null && !bookDTO.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(bookDTO.getCategoryIds());
            book.setCategories(new HashSet<>(categories));
        }

        Book savedBook = bookRepository.save(book);
        return modelMapper.map(savedBook, BookDTO.class);
    }

    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        modelMapper.map(bookDTO, book);

        if (bookDTO.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDTO.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + bookDTO.getAuthorId()));
            book.setAuthor(author);
        }

        if (bookDTO.getCategoryIds() != null && !bookDTO.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(bookDTO.getCategoryIds());
            book.setCategories(new HashSet<>(categories));
        }

        Book updatedBook = bookRepository.save(book);
        return modelMapper.map(updatedBook, BookDTO.class);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        bookRepository.delete(book);
    }
}


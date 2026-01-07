package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.example.kortex.users.db.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;


@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByEmailEqualsIgnoreCase(email);
        notFoundUser(user);
        return user;
    }

    public User getCurrentUserCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByIdWithCart(email);
        notFoundUser(user);
        return user;
    }

    public User getCurrentUserOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByIdWithOrders(email);
        notFoundUser(user);
        return user;
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    public User getByEmail(String email) {
        log.info("Поиск пользователя с email={}", email);

        User user = userRepository.findByEmailEqualsIgnoreCase(email);

        if (user == null) {
            log.debug("Пользователь не найден с email={}", email);
        } else {
            log.debug("Пользователь найден с email={}", user.getEmail());
        }
        return user;
    }

    public User create(User userToCreate) {
        try {
            log.info("Создания пользователя");
            User createdUser = userRepository.save(userToCreate);
            log.info("Пользователь создан его id={}", createdUser.getId());
            return createdUser;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Пользователь с email " + userToCreate.getEmail() + " уже существует");
        }
    }

    @Transactional
    public User update(Long id, User userToUpdate) {
        log.info("Обновление пользователя с id={}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        User updatedUser = new User(
                user.getId(),
                userToUpdate.getEmail(),
                userToUpdate.getName(),
                userToUpdate.getPassword(),
                userToUpdate.getRole(),
                userToUpdate.getAddress(),
                user.getOrders(),
                user.getCart(),
                userToUpdate.getRoleRequests());

        User savedUser = userRepository.save(updatedUser);
        log.info("Пользователь обновлен с id={}", savedUser.getId());
        return userRepository.save(savedUser);
    }


    private static void notFoundUser(User user) {
        if(user == null) {
            log.error("Авторизованный пользователь не найдет");
            throw new IllegalArgumentException("Не найден пользователь");
        }
    }
}

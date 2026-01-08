package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.api.dto.OrderMapper;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.db.Order;
import org.example.kortex.users.api.dto.user.UserMapper;
import org.example.kortex.users.api.dto.user.UserResponse;
import org.example.kortex.users.db.User;
import org.example.kortex.users.db.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.util.List;


@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, OrderMapper orderMapper, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    //================================Controller Methods================================================


    public UserResponse getProfile() {
        return userMapper.toDto(getCurrentUser());
    }

    public UserResponse changeAddress(String newAddress) {
        try {
            User user = getCurrentUser();
            log.info("Обновление адреса у пользователя id={}", user.getId());
            user.setAddress(newAddress);
            return userMapper.toDto(userRepository.save(user));
        }catch (Exception  e){
            log.error("Ошибка при обновление адреса, ex={}", e.getMessage());
            throw new RuntimeException("Не удалось обновить пароль", e);
        }
    }

    public List<OrderResponseDTO> meOrders() {
        try {
            User user = getCurrentUserOrders();
            List<Order> userOrders = user.getOrders();
            return orderMapper.toDtoList(userOrders);
        }
        catch (Exception e) {
            log.error("Не удалось загрузить заказы пользователя, ex={}", e.getMessage());
            throw new IllegalStateException("Не удалось загрузить заказы пользователя", e);
        }
    }

    //================================Service Methods================================================

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
            log.error("Ошибка создание пользователя, ex={}", e.getMessage());
            throw new RuntimeException("Пользователь с email " + userToCreate.getEmail() + " уже существует");
        }
    }

    @Transactional
    public User update(Long id, User userToUpdate) {
        try {
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
        catch (DataIntegrityViolationException e) {
            log.error("Ошибка обновление пользователя id={}, ex={}",id ,e.getMessage());
            throw new RuntimeException("Ошибка обновление пользователя",e);
        }
    }

    @Transactional
    public User changePassword(Long id, String newPassword) {
        try {
            log.info("Обновление пароля у пользователя с id={}", id);
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            user.setPassword(newPassword);

            User savedUser = userRepository.save(user);
            log.info("Пароль пользователя обновлен с id={}", savedUser.getId());
            return userRepository.save(savedUser);
        }catch (Exception e) {
            log.error("Ошибка смена пароля пользователя id={}, ex={}", id ,e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось изменить пароль"
            );
        }
    }


    private static void notFoundUser(User user) {
        if(user == null) {
            log.error("Авторизованный пользователь не найдет");
            throw new IllegalArgumentException("Не найден пользователь");
        }
    }
}

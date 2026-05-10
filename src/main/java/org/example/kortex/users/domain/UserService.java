package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.domain.mapper.OrderMapper;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.db.Order;
import org.example.kortex.users.api.dto.user.UserResponse;
import org.example.kortex.users.domain.mapper.UserMapper;
import org.example.kortex.users.db.User;
import org.example.kortex.users.db.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return userMapper.convertEntityToDto(getCurrentUser());
    }

    @Transactional
    public UserResponse changeAddress(String newAddress) {
        try {
            if(newAddress == null){
                throw new IllegalArgumentException("Пустой адрес,укажите правильно адрес");
            }
            User user = getCurrentUser();
            user.setAddress(newAddress);
            return userMapper.convertEntityToDto(userRepository.save(user));
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
        if (email == null) {
            throw new IllegalArgumentException("Пустой email пользователя");
        }
        return userRepository.findByEmailEqualsIgnoreCase(email);
    }

    public User create(User userToCreate) {
        try {
            return userRepository.save(userToCreate);
        } catch (Exception e) {
            log.error("Ошибка создание пользователя, ex={}", e.getMessage());
            throw new RuntimeException("Пользователь с email " + userToCreate.getEmail() + " уже существует");
        }
    }

    @Transactional
    public User update(Long id, User userToUpdate) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            User updatedUser = User.builder()
                    .id(user.getId())
                    .email(userToUpdate.getEmail())
                    .name(userToUpdate.getName())
                    .password(userToUpdate.getPassword())
                    .address(userToUpdate.getAddress())
                    .role(userToUpdate.getRole())
                    .orders(user.getOrders())
                    .cart(user.getCart())
                    .roleRequests(user.getRoleRequests())
                    .build();

            User savedUser = userRepository.save(updatedUser);
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
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            user.setPassword(newPassword);

            User savedUser = userRepository.save(user);
            log.info("Пароль пользователя обновлен с id={}", savedUser.getId());
            return userRepository.save(savedUser);
        }catch (Exception e) {
            log.error("Ошибка смена пароля пользователя id={}, ex={}", id ,e.getMessage());
            throw new RuntimeException("Не удалось изменить пароль, ex=" + e.getMessage());
        }
    }


    private static void notFoundUser(User user) {
        if(user == null) {
            log.error("Авторизованный пользователь не найдет");
            throw new IllegalArgumentException("Не найден пользователь");
        }
    }
}

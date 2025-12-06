package org.example.kortex.users.domain;

import org.example.kortex.users.db.User;
import org.example.kortex.users.db.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();//Вернет логин
        User user =  userRepository.findByEmailEqualsIgnoreCase(email);
        if(user == null) {
            throw new IllegalArgumentException("Не найден пользователь");
        }
        return user;
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    public User appoint(Long userId, User.Role role) {
        User user = getById(userId);
        if(User.Role.COURIER.equals(role)) {
            if(user.getRole().equals(User.Role.ADMIN) ||
                    user.getRole().equals(User.Role.SELLER)) {
                throw new IllegalArgumentException(
                        "Нельзя назначить курьером пользователя с ролью: " + user.getRole()
                );
            }
        }
        if(User.Role.SELLER.equals(role)) {
            if(user.getRole().equals(User.Role.ADMIN) ||
                    user.getRole().equals(User.Role.COURIER)) {
                throw new IllegalArgumentException(
                        "Нельзя назначить продавцом пользователя с ролью: " + user.getRole()
                );
            }
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    public User downgrade(Long userId, User.Role role) {
        User user = getById(userId);

        if (!user.getRole().equals(role)) {
            throw new IllegalArgumentException(
                    "Нельзя забрать роль " + role + " у пользователя с ролью: " + user.getRole()
            );
        }

        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getByEmail(String email) {
        User user = userRepository.findByEmailEqualsIgnoreCase(email);
        return user;
    }

    public User create(User userToCreate) {
        try {
            return userRepository.save(userToCreate);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Пользователь с email " + userToCreate.getEmail() + " уже существует");
        }
    }

    public User update(Long id, User userToUpdate) {
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
        return userRepository.save(updatedUser);
    }

}

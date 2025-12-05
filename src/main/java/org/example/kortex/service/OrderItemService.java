package org.example.kortex.service;

import javax.persistence.EntityNotFoundException;
import org.example.kortex.entity.*;
import org.example.kortex.repository.OrderItemRepository;
import org.example.kortex.repository.OrderRepository;
import org.example.kortex.repository.ProductRepository;
import org.example.kortex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
            return savedItems;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить элементы заказа", e);
        }
    }

    public OrderItem findById(Long id) {
        return orderItemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("OrderItem не найден"));
    }

    public List<CartItem> findAllByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

}

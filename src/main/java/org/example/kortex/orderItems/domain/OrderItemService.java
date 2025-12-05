package org.example.kortex.orderItems.domain;

import javax.persistence.EntityNotFoundException;

import org.example.kortex.cartItems.db.CartItem;
import org.example.kortex.orderItems.db.OrderItem;
import org.example.kortex.orderItems.db.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


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

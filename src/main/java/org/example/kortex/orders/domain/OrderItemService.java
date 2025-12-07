package org.example.kortex.orders.domain;

import javax.persistence.EntityNotFoundException;

import org.example.kortex.carts.db.CartItem;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderItemRepository;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        log.info("Сохранения всех orderItem");
        if (orderItems == null || orderItems.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
            log.info("Успешно сохранения всех orderItem");
            return savedItems;
        } catch (Exception e) {
            log.error("Не удалось сохранить элементы заказа" + e.getMessage());
            throw new RuntimeException("Не удалось сохранить элементы заказа", e);
        }
    }

}

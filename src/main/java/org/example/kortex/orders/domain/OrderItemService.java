package org.example.kortex.orders.domain;


import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    //================================Controller Methods================================================

    //================================Service Methods================================================


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
            log.error("Не удалось сохранить элементы заказа, ex={}",e.getMessage());
            throw new RuntimeException("Не удалось сохранить элементы заказа", e);
        }
    }
}

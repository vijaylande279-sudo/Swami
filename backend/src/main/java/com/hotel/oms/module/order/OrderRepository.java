package com.hotel.oms.module.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem " +
           "LEFT JOIN FETCH o.table WHERE o.id = :id")
    Optional<Order> findByIdWithItems(Long id);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem " +
           "LEFT JOIN FETCH o.table WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<Order> findByStatusWithItems(OrderStatus status);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem " +
           "LEFT JOIN FETCH o.table WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findByStatusInWithItems(List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.table.id IN :tableIds AND o.status <> com.hotel.oms.module.order.OrderStatus.CLOSED")
    List<Order> findActiveByTableIdIn(List<Long> tableIds);

    boolean existsByTableId(Long tableId);
}

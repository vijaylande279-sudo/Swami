package com.hotel.oms.module.table;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<DiningTable, Long> {
    boolean existsByTableNumber(String tableNumber);
}

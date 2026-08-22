package com.hotel.oms.module.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("SELECT m FROM MenuItem m LEFT JOIN FETCH m.category")
    List<MenuItem> findAllWithCategory();
}

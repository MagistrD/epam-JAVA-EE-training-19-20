package com.epam.training.repository;

import com.epam.training.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findAllByName(String name, Pageable pageable);

    @Query("SELECT prod FROM Price pr INNER JOIN Product prod ON prod.id = pr.product.id"
            + " WHERE pr.amount LIKE :price")
    Page<Product> findAllByPrice(@Param("price") BigDecimal price,
                                 Pageable pageable);

    @Query("SELECT pr FROM Category ct INNER JOIN Product pr ON pr.id = ct.id WHERE ct.id LIKE :categoryID")
    Page<Product> findAllByCategoryID(@Param("categoryID") Integer categoryID, Pageable pageable);
}

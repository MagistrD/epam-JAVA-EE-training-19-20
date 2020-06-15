package com.epam.training.repository;

import com.epam.training.model.Price;
import com.epam.training.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Integer> {
    List<Price> findAllByCurrency(String currency, Pageable pageable);

    List<Price> findAllByProduct(Product product, Pageable pageable);

    List<Price> findAllByAmountBetweenAndCurrency(BigDecimal from, BigDecimal to, String currency, Pageable pageable);
}

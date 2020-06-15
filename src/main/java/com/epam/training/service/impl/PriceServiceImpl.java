package com.epam.training.service.impl;

import com.epam.training.model.Price;
import com.epam.training.model.Product;
import com.epam.training.repository.PriceRepository;
import com.epam.training.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PriceServiceImpl implements PriceService {

    @Autowired
    private PriceRepository priceRepository;

    public PriceServiceImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Price save(Price price) {
        return priceRepository.save(price);
    }

    @Override
    public String findAll() {
        return String.valueOf(priceRepository.findAll());
    }

    @Override
    public Optional<Price> findById(Integer id) {
        return priceRepository.findById(id);
    }

    @Override
    public List<Price> findByRange(BigDecimal from, BigDecimal to, String currency) {
        return priceRepository.findAllByAmountBetweenAndCurrency(from, to, currency, Pageable.unpaged());
    }


    @Override
    public List<Price> findByCurrency(String currency) {
        return priceRepository.findAllByCurrency(currency, Pageable.unpaged());
    }

    @Override
    public List<Price> findByProduct(Product product) {
        return priceRepository.findAllByProduct(product, Pageable.unpaged());
    }

    @Override
    public void delete(Integer id) {
        priceRepository.deleteById(id);
    }

}

package com.epam.training.service.impl;

import com.epam.training.model.Product;
import com.epam.training.repository.ProductRepository;
import com.epam.training.service.CRUDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductService implements CRUDService<Product> {

    @Autowired
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product save(Product product) {
        if (!product.getName().isEmpty())
            return productRepository.save(product);
        else
            return null;
    }

    @Override
    public Product findById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product update(Product product) {
        return productRepository.saveAndFlush(product);
    }

    @Override
    public void delete(Integer id) {
        productRepository.deleteById(id);
    }

    public Page<Product> findByName(String name, Pageable pageable) {
        return productRepository.findAllByName(name, pageable);
    }

    public Page<Product> findByPrice(BigDecimal price, Pageable pageable) {
        return productRepository.findAllByPrice(price, pageable);
    }

    public Page<Product> findByCategoryID(Integer category, Pageable pageable) {
        return productRepository.findAllByCategoryID(category, pageable);
    }


}

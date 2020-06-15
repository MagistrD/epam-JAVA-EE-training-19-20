package com.epam.training.generator;

import com.epam.training.model.Category;
import com.epam.training.model.Price;
import com.epam.training.model.Product;
import com.epam.training.repository.CategoryRepository;
import com.epam.training.repository.ProductRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Transactional
@Component
public class CatalogGenerator {
    Logger logger = Logger.getLogger(CatalogGenerator.class);

    private static final int PRODUCT_COUNT = 100;
    private static final int CATEGORY_COUNT = 10;
    private static final Random RANDOM = new Random();

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CatalogGenerator(final ProductRepository productRepository, final CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public void catalog() {
        categories();
        products();
    }

    private void categories() {
        categoryRepository.save(new Category("root"));
        List<Category> categoryList = new ArrayList<>();
        for (int i = 1; i < CATEGORY_COUNT; i++) {
            categoryList.add(new Category("category_" + i));
        }
        categoryRepository.saveAll(categoryList);
        categoryList.forEach(x -> x.setParent(categoryRepository.findById(rnd(CATEGORY_COUNT)).orElseThrow()));
        categoryRepository.saveAll(categoryList);
    }

    private void products() {
        final List<Product> productList = new ArrayList<>();
        for (int i = 0; i < PRODUCT_COUNT; i++) {

            final Price byn = new Price(BigDecimal.valueOf(rnd(1000)), "BYN");
            final Price eur = new Price(BigDecimal.valueOf(rnd(1000)), "EUR");
            final Price usd = new Price(BigDecimal.valueOf(rnd(1000)), "USD");

            final Product product = new Product();
            product.setName("product_" + i);

            product.getPrices().add(byn);
            product.getPrices().add(eur);
            product.getPrices().add(usd);

            byn.setProduct(product);
            eur.setProduct(product);
            usd.setProduct(product);

            product.getCategories().add(categoryRepository.findById(rnd(PRODUCT_COUNT)).orElseThrow());
            product.getCategories().add(categoryRepository.findById(rnd(PRODUCT_COUNT)).orElseThrow());
            product.getCategories().add(categoryRepository.findById(rnd(PRODUCT_COUNT)).orElseThrow());

            logger.info(product.toString());
            productList.add(product);
        }
        productRepository.saveAll(productList);
    }

    private int rnd(final int max) {
        return RANDOM.nextInt(max - 1) + 1;
    }
}

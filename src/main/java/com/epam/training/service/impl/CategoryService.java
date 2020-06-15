package com.epam.training.service.impl;

import com.epam.training.model.Category;
import com.epam.training.repository.CategoryRepository;
import com.epam.training.service.CRUDService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService implements CRUDService<Category> {
    Logger logger = Logger.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category update(Category category) {
        return categoryRepository.saveAndFlush(category);
    }

    @Override
    public void delete(Integer id) {
        try {
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public Page<Category> findByName(String name, Pageable pageable) {
        return categoryRepository.findAllByName(name, pageable);
    }
}

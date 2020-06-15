package com.epam.training.repository;

import com.epam.training.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
@DataJdbcTest
public class ControllerRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void findByNameTest() {
        categoryRepository.save(new Category("Test category"));
        Page<Category> categories = categoryRepository.findAllByName("Test category", Pageable.unpaged());
        then(categories.getContent().get(0).getName()).isEqualTo("Test category");
    }
}

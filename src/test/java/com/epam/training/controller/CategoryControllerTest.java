package com.epam.training.controller;

import com.epam.training.dto.CategoryDto;
import com.epam.training.service.impl.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CategoryControllerTest {

    private static final String CATEGORY = "category";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CategoryService service;

    private final static String URL_API = "/category";

    @Test
    public void testCreateCategory() throws Exception {
        final CategoryDto category = CategoryDto.builder().name(CATEGORY).parent(1)
                .build();
        mockMvc.perform(post(URL_API)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(CategoryDto.builder().name(CATEGORY).parent(1)
                        .build())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(category.getName())))
                .andExpect(jsonPath("$.parent", is(category.getParent())));
    }

    @Test
    public void testReadAllCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_API))
                .andExpect(MockMvcResultMatchers.view().name("categories"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    public void testDeleteCategory() throws Exception {
        mockMvc.perform(delete(URL_API + "/{id}", 1))
                .andExpect(status().isOk());
        mockMvc.perform(get(URL_API + "/{id}", 1))
                .andExpect(status().isNotFound());
    }
}

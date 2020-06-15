package com.epam.training.conroller;

import com.epam.training.dto.CategoryDto;
import com.epam.training.model.Category;
import com.epam.training.model.CategorySearchCriteria;
import com.epam.training.model.Pager;
import com.epam.training.service.impl.CategoryService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("category")
public class CategoryController {
    Logger logger = Logger.getLogger(CategoryController.class);

    private static final int BUTTONS_TO_SHOW = 5;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 10;
    private static final int[] PAGE_SIZES = {5, 10, 20};

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ModelAndView getCategories(@RequestParam("pageSize") Optional<Integer> pageSize,
                                      @RequestParam("page") Optional<Integer> page) {
        ModelAndView modelAndView = new ModelAndView("categories");
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        Page<Category> categories = categoryService.findAll(PageRequest.of(evalPage, evalPageSize));
        Pager pager = new Pager(categories.getTotalPages(), categories.getNumber(), BUTTONS_TO_SHOW);
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }

    @GetMapping("/drop")
    public List<Category> getCategory() {
        List<Category> categories = categoryService.findAll(Pageable.unpaged()).getContent();
        return categories;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        Category category = categoryService.findById(id);
        if (category != null) {
            return new ResponseEntity<Category>(category, HttpStatus.OK);
        } else return new ResponseEntity<Category>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public Category createCategory(@RequestBody CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        Category parent = categoryService.findById(categoryDto.getParent());
        category.setParent(parent);
        return categoryService.save(category);
    }

    @PutMapping("{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        Category categoryForUpdate = categoryService.findById(id);
        if (categoryForUpdate != null) {
            category.setId(categoryForUpdate.getId());
            categoryService.update(category);
            return new ResponseEntity<Category>(HttpStatus.OK);
        }
        return new ResponseEntity<Category>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Integer> deleteCategory(@PathVariable Integer id) {
        Category categoryForDelete = categoryService.findById(id);
        if (categoryForDelete != null) {
            categoryService.delete(id);
            return new ResponseEntity<Integer>(id, HttpStatus.OK);
        }
        return new ResponseEntity<Integer>(id, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/search")
    public List<Category> getCategoryBy(@RequestBody CategorySearchCriteria search) {
        List<Category> categories;
        categories = findBy(search.id, search.name);
        return categories;
    }

    private List<Category> findBy(String id, String name) {
        List<Category> result = new ArrayList<>();
        if (!StringUtils.isEmpty(id)) {
            result.add(categoryService.findById(Integer.valueOf(id)));
        }
        if (!StringUtils.isEmpty(name)) {
            for (Category category : categoryService.findByName(name, Pageable.unpaged()))
                result.add(category);
        }
        return result;
    }
}

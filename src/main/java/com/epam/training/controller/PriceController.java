package com.epam.training.controller;

import com.epam.training.model.Price;
import com.epam.training.service.impl.PriceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/price")
public class PriceController {

    @Autowired
    private PriceServiceImpl priceServiceImpl;

    @PostMapping("/create")
    public void createPrice(@RequestBody Price price) {
        priceServiceImpl.save(price);
    }
}

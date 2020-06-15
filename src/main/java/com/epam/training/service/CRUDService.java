package com.epam.training.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CRUDService<M> {

    M save(M m);

    M findById(Integer id);

    Page<M> findAll(Pageable pageable);

    M update(M m);

    void delete(Integer id);
}

package com.epam.training.service;

import com.epam.training.model.Price;
import com.epam.training.model.Product;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebService(serviceName = "PriceCrudService")
public interface PriceService {

    @WebMethod()
    @WebResult(name = "savePrice")
    Price save(@WebParam(name = "price") Price price);

    @WebMethod()
    @WebResult(name = "prices")
    String findAll();

    @WebMethod()
    @WebResult(name = "price")
    Optional<Price> findById(@WebParam(name = "id") Integer id);

    @WebMethod()
    @WebResult(name = "pricesByRange")
    List<Price> findByRange(@WebParam(name = "from") BigDecimal from,
                            @WebParam(name = "to") BigDecimal to,
                            @WebParam(name = "currency") String currency);

    @WebMethod()
    List<Price> findByCurrency(@WebParam(name = "currency") String currency);

    @WebMethod()
    List<Price> findByProduct(@WebParam(name = "product") Product product);

    @WebMethod()
    void delete(@WebParam(name = "id") Integer id);

//    Price findById(Integer id);
//
//    Page<Price> findAll(Pageable pageable);
//
//    void update(Price m);
//
//    void delete(Integer id);
}
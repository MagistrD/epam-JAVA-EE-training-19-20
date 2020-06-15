package com.epam.training.controller;

import com.epam.training.config.WSSecurityCallback;
import com.epam.training.dto.PriceRangeDto;
import com.epam.training.dto.ProductDto;
import com.epam.training.model.*;
import com.epam.training.service.PriceService;
import com.epam.training.service.impl.CategoryService;
import com.epam.training.service.impl.PriceServiceImpl;
import com.epam.training.service.impl.ProductService;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.log4j.Logger;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;

@RestController
@RequestMapping("product")
public class ProductController {
    Logger logger = Logger.getLogger(ProductController.class);

    private static final int BUTTONS_TO_SHOW = 5;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 10;
    private static final int[] PAGE_SIZES = {5, 10, 20};

    @Autowired
    private ProductService productService;
    @Autowired
    private PriceServiceImpl priceServiceImpl;
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ModelAndView getProducts(@RequestParam("pageSize") Optional<Integer> pageSize,
                                    @RequestParam("page") Optional<Integer> page) {
        ModelAndView modelAndView = new ModelAndView("products");
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        Page<Product> products = productService.findAll(PageRequest.of(evalPage, evalPageSize));
        Pager pager = new Pager(products.getTotalPages(), products.getNumber(), BUTTONS_TO_SHOW);
        modelAndView.addObject("products", products);
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }

    @PostMapping("/searchByRange")
    public List<Product> getPricesByPriceRange(@RequestBody PriceRangeDto priceRangeDto) {
        List<Price> prices = priceServiceImpl.findByRange(priceRangeDto.getFrom(), priceRangeDto.getTo(), priceRangeDto.getCurrency());

        List<Product> products = new ArrayList<>();
        for (Price price : prices) {
            products.add(price.getProduct());
        }
        return products;
    }

    @PostMapping("/search")
    public List<Product> getProductBy(@RequestBody ProductSearchCriteria search) {
        List<Product> productList = new ArrayList<>();
        productList = findBy(search.id, search.name, search.price, search.categoryID);
        return productList;

    }

    @Transactional
    @PostMapping()
    public void createProduct(@RequestBody ProductDto productDto) throws MalformedURLException {
        Product product = new Product();
        product = readDataFromDto(product, productDto);
        productService.save(product);
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("user", "123".toCharArray());
            }
        });
        URL url = new URL("http://localhost:8088/services/priceService?wsdl");
        QName qname = new QName("http://impl.service.training.epam.com/", "PriceServiceImplService");
        Service service = Service.create(url, qname);
        PriceService priceService = service.getPort(PriceService.class);

        Client client = ClientProxy.getClient(priceService);
        Endpoint endpoint = client.getEndpoint();

        Map props = new HashMap();
        props.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        props.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        props.put(WSHandlerConstants.PW_CALLBACK_CLASS, WSSecurityCallback.class.getName());
        props.put(WSHandlerConstants.USER, "user");

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(props);
        endpoint.getOutInterceptors().add(wssOut);

        priceService.save(product.getPrices().get(0));
        priceService.save(product.getPrices().get(1));
        priceService.save(product.getPrices().get(2));
        logger.info("Created");
    }


    @GetMapping("{id}")
    public Product getProductById(@PathVariable Integer id) {
        return productService.findById(id);
    }

    @PutMapping()
    public Product updateProduct(@RequestBody ProductDto productDto) {
        Product productForUpdate = productService.findById(productDto.getId());

        if (productForUpdate != null) {
            readDataFromDto(productForUpdate, productDto);
            productService.save(productForUpdate);
        }
        return productForUpdate;
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Integer> deleteProduct(@PathVariable Integer id) {
        Product productForDelete = productService.findById(id);
        if (productForDelete != null) {
            productService.delete(id);
            return new ResponseEntity<>(id, HttpStatus.OK);
        }
        return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
    }

    private Product readDataFromDto(Product product, ProductDto productDto) {
        product.setName(productDto.getName());
        List<Category> categories = new ArrayList<>();
        categories.add(categoryService.findById(productDto.getCategory1()));
        categories.add(categoryService.findById(productDto.getCategory2()));
        categories.add(categoryService.findById(productDto.getCategory3()));
        product.setCategories(categories);

        Price price1 = new Price(productDto.getAmount1(), "BYN");
        price1.setProduct(product);
        Price price2 = new Price(productDto.getAmount2(), "EUR");
        price2.setProduct(product);
        Price price3 = new Price(productDto.getAmount3(), "USD");
        price3.setProduct(product);

        List<Price> prices = new ArrayList<>();
        prices.add(price1);
        prices.add(price2);
        prices.add(price3);
        product.setPrices(prices);
        return product;
    }

    private List<Product> findBy(String id, String name, BigDecimal price, String categoryID) {
        List<Product> result = new ArrayList<>();
        if (!StringUtils.isEmpty(id)) {
            result.add(productService.findById(Integer.valueOf(id)));
        }
        if (!StringUtils.isEmpty(name)) {
            for (Product product : productService.findByName(name, Pageable.unpaged()))
                result.add(product);
        }
        if (!StringUtils.isEmpty(categoryID)) {
            for (Product product : productService.findByCategoryID(Integer.valueOf(categoryID), Pageable.unpaged()))
                result.add(product);
        }
        return result;
    }
}

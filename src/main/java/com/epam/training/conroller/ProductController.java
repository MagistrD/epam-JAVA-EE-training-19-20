package com.epam.training.conroller;

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
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
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
        return productForUpdate;K6IXATEF43-eyJsaWNlbnNlSWQiOiJLNklYQVRFRjQzIiwibGljZW5zZWVOYW1lIjoi5o6I5p2D5Luj55CG5ZWGOiBodHRwOi8vaWRlYS5oay5jbiIsImFzc2lnbmVlTmFtZSI6IiIsImFzc2lnbmVlRW1haWwiOiIiLCJsaWNlbnNlUmVzdHJpY3Rpb24iOiIiLCJjaGVja0NvbmN1cnJlbnRVc2UiOmZhbHNlLCJwcm9kdWN0cyI6W3siY29kZSI6IklJIiwiZmFsbGJhY2tEYXRlIjoiMjAxOS0wNi0wNSIsInBhaWRVcFRvIjoiMjAyMC0wNi0wNCJ9LHsiY29kZSI6IkFDIiwiZmFsbGJhY2tEYXRlIjoiMjAxOS0wNi0wNSIsInBhaWRVcFRvIjoiMjAyMC0wNi0wNCJ9LHsiY29kZSI6IkRQTiIsImZhbGxiYWNrRGF0ZSI6IjIwMTktMDYtMDUiLCJwYWlkVXBUbyI6IjIwMjAtMDYtMDQifSx7ImNvZGUiOiJQUyIsImZhbGxiYWNrRGF0ZSI6IjIwMTktMDYtMDUiLCJwYWlkVXBUbyI6IjIwMjAtMDYtMDQifSx7ImNvZGUiOiJHTyIsImZhbGxiYWNrRGF0ZSI6IjIwMTktMDYtMDUiLCJwYWlkVXBUbyI6IjIwMjAtMDYtMDQifSx7ImNvZGUiOiJETSIsImZhbGxiYWNrRGF0ZSI6IjIwMTktMDYtMDUiLCJwYWlkVXBUbyI6IjIwMjAtMDYtMDQifSx7ImNvZGUiOiJDTCIsImZhbGxiYWNrRGF0ZSI6IjIwMTktMDYtMDUiLCJwYWlkVXBUbyI6IjIwMjAtMDYtMDQifSx7ImNvZGUiOiJSUzAiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiUkMiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiUkQiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiUEMiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiUk0iLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiV1MiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiREIiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiREMiLCJmYWxsYmFja0RhdGUiOiIyMDE5LTA2LTA1IiwicGFpZFVwVG8iOiIyMDIwLTA2LTA0In0seyJjb2RlIjoiUlNVIiwiZmFsbGJhY2tEYXRlIjoiMjAxOS0wNi0wNSIsInBhaWRVcFRvIjoiMjAyMC0wNi0wNCJ9XSwiaGFzaCI6IjEzMjkyMzI4LzAiLCJncmFjZVBlcmlvZERheXMiOjcsImF1dG9Qcm9sb25nYXRlZCI6ZmFsc2UsImlzQXV0b1Byb2xvbmdhdGVkIjpmYWxzZX0=-KUaQi549fH96M/qU7jTvuMeq2GuedA+WppV3irI0JHlfDuhJlidK2m3yoRxitGNmimPFVUA8Dk38OzXnP29I39QDXH5VAF8VjOP0XrqdfrpaZUKpdhRaYz8r1NAwID75U4LqYCvFbazka1dCMJBFqJ2wum1+CSQhJ1O7CSchAJAbjcCRQjbU2sXOofAA2sPLi7nlJw2wrjOHzH9cOczUn11n24PE9BQ/oYGITHkzsu94i4Q90Z1jQysMtXLgM/HoLSHY2T9rKULLoh+tdMwBp9+m0VLF/R5gdkVDV/dlorrA9OEZIsSOaG+oWSen/AulKH6OXllZJoR+b/T6YYfGWg==-MIIElTCCAn2gAwIBAgIBCTANBgkqhkiG9w0BAQsFADAYMRYwFAYDVQQDDA1KZXRQcm9maWxlIENBMB4XDTE4MTEwMTEyMjk0NloXDTIwMTEwMjEyMjk0NlowaDELMAkGA1UEBhMCQ1oxDjAMBgNVBAgMBU51c2xlMQ8wDQYDVQQHDAZQcmFndWUxGTAXBgNVBAoMEEpldEJyYWlucyBzLnIuby4xHTAbBgNVBAMMFHByb2QzeS1mcm9tLTIwMTgxMTAxMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxcQkq+zdxlR2mmRYBPzGbUNdMN6OaXiXzxIWtMEkrJMO/5oUfQJbLLuMSMK0QHFmaI37WShyxZcfRCidwXjot4zmNBKnlyHodDij/78TmVqFl8nOeD5+07B8VEaIu7c3E1N+e1doC6wht4I4+IEmtsPAdoaj5WCQVQbrI8KeT8M9VcBIWX7fD0fhexfg3ZRt0xqwMcXGNp3DdJHiO0rCdU+Itv7EmtnSVq9jBG1usMSFvMowR25mju2JcPFp1+I4ZI+FqgR8gyG8oiNDyNEoAbsR3lOpI7grUYSvkB/xVy/VoklPCK2h0f0GJxFjnye8NT1PAywoyl7RmiAVRE/EKwIDAQABo4GZMIGWMAkGA1UdEwQCMAAwHQYDVR0OBBYEFGEpG9oZGcfLMGNBkY7SgHiMGgTcMEgGA1UdIwRBMD+AFKOetkhnQhI2Qb1t4Lm0oFKLl/GzoRykGjAYMRYwFAYDVQQDDA1KZXRQcm9maWxlIENBggkA0myxg7KDeeEwEwYDVR0lBAwwCgYIKwYBBQUHAwEwCwYDVR0PBAQDAgWgMA0GCSqGSIb3DQEBCwUAA4ICAQAF8uc+YJOHHwOFcPzmbjcxNDuGoOUIP+2h1R75Lecswb7ru2LWWSUMtXVKQzChLNPn/72W0k+oI056tgiwuG7M49LXp4zQVlQnFmWU1wwGvVhq5R63Rpjx1zjGUhcXgayu7+9zMUW596Lbomsg8qVve6euqsrFicYkIIuUu4zYPndJwfe0YkS5nY72SHnNdbPhEnN8wcB2Kz+OIG0lih3yz5EqFhld03bGp222ZQCIghCTVL6QBNadGsiN/lWLl4JdR3lJkZzlpFdiHijoVRdWeSWqM4y0t23c92HXKrgppoSV18XMxrWVdoSM3nuMHwxGhFyde05OdDtLpCv+jlWf5REAHHA201pAU6bJSZINyHDUTB+Beo28rRXSwSh3OUIvYwKNVeoBY+KwOJ7WnuTCUq1meE6GkKc4D/cXmgpOyW/1SmBz3XjVIi/zprZ0zf3qH5mkphtg6ksjKgKjmx1cXfZAAX6wcDBNaCL+Ortep1Dh8xDUbqbBVNBL4jbiL3i3xsfNiyJgaZ5sX7i8tmStEpLbPwvHcByuf59qJhV/bZOl8KqJBETCDJcY6O2aqhTUy+9x93ThKs1GKrRPePrWPluud7ttlgtRveit/pcBrnQcXOl1rHq7ByB8CFAxNotRUYL9IF5n3wJOgkPojMy6jetQA5Ogc8Sm7RG6vg1yow==
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

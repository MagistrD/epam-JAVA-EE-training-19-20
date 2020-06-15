package com.epam.training.config;

import com.epam.training.service.PriceService;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWs
public class WebServicesConfig {
    @Autowired
    private PriceService priceService; // your web service component

    @Bean
    public ServletRegistrationBean wsDispatcherServlet() {
        CXFServlet cxfServlet = new CXFServlet();
        return new ServletRegistrationBean(cxfServlet, "/services/*");
    }

    @Bean(name="cxf")
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public EndpointImpl helloWorldEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), priceService);
        endpoint.publish("/priceService");

        Endpoint cxfEndPoint = endpoint.getServer().getEndpoint();

        Map inProps = new HashMap<>();
        inProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
        inProps.put(ConfigurationConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        inProps.put(ConfigurationConstants.PW_CALLBACK_CLASS, WSSecurityCallback.class.getName());

        WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
        cxfEndPoint.getInInterceptors().add(wssIn);

        return endpoint;
    }
}
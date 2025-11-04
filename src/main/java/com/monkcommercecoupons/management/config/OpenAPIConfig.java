package com.monkcommercecoupons.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI couponManagementAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("Monk Commerce Coupon Management Assignment");
        contact.setEmail("misbah.almas@hotmail.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Coupons Management API")
                .version("1.0.0")
                .description("RESTful API for managing and applying discount coupons for an e-commerce platform. " +
                        "Supports cart-wise, product-wise, and BxGy (Buy X Get Y) coupon types.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}

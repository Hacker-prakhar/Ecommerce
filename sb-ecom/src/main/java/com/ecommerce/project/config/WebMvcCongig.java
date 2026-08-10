package com.ecommerce.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcCongig implements WebMvcConfigurer {

    @Value("${project.image}")
    private String imagePath; // get the folder name i.e /images

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**") // any request coming from  / image api
                .addResourceLocations("file:" + imagePath); // return file in this location
    }
}

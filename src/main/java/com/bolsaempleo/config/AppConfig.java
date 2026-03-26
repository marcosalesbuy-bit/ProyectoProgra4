package com.bolsaempleo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * MVC configuration: exposes the CV upload directory as a static resource
 * so uploaded PDFs can be served under /uploads/cv/<filename>.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${app.cv.upload-dir}")
    private String cvUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Make sure the directory exists
        new File(cvUploadDir).mkdirs();

        // Serve uploaded CVs as static resources
        registry.addResourceHandler("/uploads/cv/**")
                .addResourceLocations("file:" + cvUploadDir + "/");
    }
}

package org.talentlink.talentlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final Path avatarDir;
    private final String publicPrefix;

    public WebMvcConfig(
            @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDir,
            @Value("${app.upload.avatar-public-prefix:/uploads/avatars}") String publicPrefix
    ) {
        this.avatarDir = Paths.get(avatarDir).toAbsolutePath().normalize();
        this.publicPrefix = normalizePrefix(publicPrefix);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String locationUri = avatarDir.toUri().toString();
        if (!locationUri.endsWith("/")) {
            locationUri += "/";
        }
        registry.addResourceHandler(publicPrefix + "/**")
                .addResourceLocations(locationUri);
    }

    private String normalizePrefix(String prefix) {
        String p = (prefix == null || prefix.isBlank()) ? "/uploads/avatars" : prefix.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return p.endsWith("/") ? p.substring(0, p.length() - 1) : p;
    }
}

package lt.metasite.task.app.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openApiConfiguration(
            @Value("${openapi.title}") final String title,
            @Value("${openapi.version}") final String version,
            @Value("${openapi.description}") final String description
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(getContact())
                );
    }

    private Contact getContact() {
        Contact contact = new Contact();
        contact.setEmail("myEmail@email.com");
        contact.setName("Mindaugas Pavardė");
        contact.setUrl("https://github.com/RimidisM");
        contact.setExtensions(Collections.emptyMap());
        return contact;
    }
}

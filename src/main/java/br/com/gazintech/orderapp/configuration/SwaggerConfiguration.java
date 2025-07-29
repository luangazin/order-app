package br.com.gazintech.orderapp.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order APP API")
                        .version("1.0.0")
                        .description("API for managing orders in the Order APP")
                        .contact(new Contact()
                                .name("Luan Ricardo Gazin")
                                .email("luan_gazin@yahoo.com.br")
                                .url("https://www.linkedin.com/in/luangazin"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server")
                ));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    operation.getResponses().entrySet().removeIf(entry ->
                            !entry.getKey().equals("200") &&
                                    !entry.getKey().equals("201") &&
                                    !entry.getKey().equals("400") &&
                                    !entry.getKey().equals("404") &&
                                    !entry.getKey().equals("418") &&
                                    !entry.getKey().equals("500"));
                })
        );
    }
}

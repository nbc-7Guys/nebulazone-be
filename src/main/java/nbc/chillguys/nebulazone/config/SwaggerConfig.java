package nbc.chillguys.nebulazone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

	private final ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		ModelConverters.getInstance().addConverter(new ModelResolver(objectMapper));
	}

	@Bean
	public OpenAPI openApi() {
		return new OpenAPI()
			.addSecurityItem(new SecurityRequirement().addList("bearer-key").addList("Refresh-Token"))
			.components(new Components()
				.addSecuritySchemes("bearer-key",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT"))
				.addSecuritySchemes("Refresh-Token",
					new SecurityScheme()
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)
						.name("Refresh-Token")
						.description("Bearer 를 붙여서 넣어주어야 합니다.")))
			.info(apiInfo());
	}

	private Info apiInfo() {
		return new Info()
			.title("NebulaZone")
			.description("NebulaZone 최종 팀 프로젝트")
			.version("1.0");
	}
}

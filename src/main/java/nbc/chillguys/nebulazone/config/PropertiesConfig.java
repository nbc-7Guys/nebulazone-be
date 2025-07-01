package nbc.chillguys.nebulazone.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import nbc.chillguys.nebulazone.infra.security.constant.JwtProperties;

@Configuration
@EnableConfigurationProperties({
	JwtProperties.class
})
public class PropertiesConfig {
}

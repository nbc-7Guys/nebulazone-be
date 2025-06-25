package nbc.chillguys.nebulazone.infra.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

	/**
	 * 한국어를 기본 로케일로 설정
	 * 애플리케이션 전체에서 한국어가 기본 언어로 사용됩니다.
	 */
	@Bean
	public LocaleResolver localeResolver() {
		FixedLocaleResolver localeResolver = new FixedLocaleResolver();
		localeResolver.setDefaultLocale(Locale.KOREA);
		return localeResolver;
	}
}

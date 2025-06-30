package nbc.chillguys.nebulazone.infra.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	/**
	 * 기본 Redis Template (기존 캐싱 및 메시지 저장용)
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		ObjectMapper objectMapper = createObjectMapper();
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(serializer);
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(serializer);

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	/**
	 * Redisson 분산 락 설정<br>
	 * 서버 CPU 2코어, 4GB 기준 - 커넥션풀: 코어 수 * 2, 최소 연결 수: 커넥션 풀의 1/4<br>
	 * redis 연결 실패 시: 1500ms 간격으로 3번 재시도<br>
	 * redis 응답이 3초 넘으면 타임아웃
	 * @author 전나겸
	 */
	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();

		String redisAddress = "redis://" + host + ":" + port;

		config.useSingleServer()
			.setAddress(redisAddress)
			.setConnectionPoolSize(20)
			.setConnectionMinimumIdleSize(5)
			.setRetryAttempts(3)
			.setRetryInterval(1500)
			.setTimeout(3000);

		return Redisson.create(config);
	}

	private ObjectMapper createObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

}

package nbc.chillguys.nebulazone.infra.redis.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
	String key();

	long waitTime() default 5;

	long leaseTime() default 10;

	TimeUnit timeUnit() default TimeUnit.SECONDS;
}

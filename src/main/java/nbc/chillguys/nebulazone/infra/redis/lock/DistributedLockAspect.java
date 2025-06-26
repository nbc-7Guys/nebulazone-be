package nbc.chillguys.nebulazone.infra.redis.lock;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

	private final RedissonClient redissonClient;
	private final ExpressionParser parser = new SpelExpressionParser();
	private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

	public DistributedLockAspect(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	@Around("@annotation(distributedLock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
		String lockKey = parseLockKey(joinPoint, distributedLock.key());
		long waitTime = distributedLock.waitTime();
		long leaseTime = distributedLock.leaseTime();
		TimeUnit timeUnit = distributedLock.timeUnit();

		RLock lock = redissonClient.getLock(lockKey);
		boolean acquired = false;
		try {
			acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
			if (!acquired) {
				throw new IllegalStateException("다른 사용자가 이미 처리 중입니다. 잠시 후 다시 시도해 주세요.");
			}
			return joinPoint.proceed();
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private String parseLockKey(ProceedingJoinPoint joinPoint, String keyExpression) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		Object[] args = joinPoint.getArgs();
		String[] paramNames = nameDiscoverer.getParameterNames(method);
		if (paramNames == null || paramNames.length == 0) {
			return keyExpression;
		}

		StandardEvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < paramNames.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}

		try {
			Expression expression = parser.parseExpression(keyExpression);
			return expression.getValue(context, String.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("잘못된 SpEL 표현식입니다: " + keyExpression, e);
		}
	}
}

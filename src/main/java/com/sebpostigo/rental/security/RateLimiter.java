package com.sebpostigo.rental.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {

	// ponytail: in-memory and one bucket per key forever — fine for one Render instance and a
	// demo's traffic; move to Bucket4j's Redis/JDBC backend (with expiry) if this ever scales out.
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	public boolean tryConsume(String key) {
		return buckets.computeIfAbsent(key, k -> newBucket()).tryConsume(1);
	}

	public void reset() {
		buckets.clear();
	}

	private static Bucket newBucket() {
		return Bucket.builder()
			.addLimit(Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(1)).build())
			.build();
	}

}

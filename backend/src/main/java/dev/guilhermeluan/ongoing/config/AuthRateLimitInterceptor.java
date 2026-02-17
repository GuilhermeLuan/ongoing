package dev.guilhermeluan.ongoing.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class AuthRateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket getOrCreateBucket(String clientId) {
        return buckets.computeIfAbsent(
                clientId, k ->
                        Bucket.builder()
                                .addLimit(limit -> limit.capacity(5).refillIntervally(5, Duration.ofMinutes(1)))
                                .build()
        );
    }

    public void resetBuckets() {
        buckets.clear();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String clientIp = request.getRemoteAddr();
        Bucket bucket = getOrCreateBucket(clientIp);

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"Too many requests\"}");
        return false;
    }


}

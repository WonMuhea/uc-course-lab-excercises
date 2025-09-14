package com.ucassignments.securesoftdev.config;

import io.github.bucket4j.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> STATIC_PREFIXES = Set.of("/css/","/js/","/images/","/h2-console/");
    @Value("${api.rate.limit}")     private int defaultCap;
    @Value("${api.rate.interval}") private int defaultWin;

    // In-memory buckets (for a single JVM). For clusters, use a distributed backend.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        for (String p : STATIC_PREFIXES) {
            if (path.startsWith(p)) { chain.doFilter(req, res); return; }
        }

        String bucketName = "ip-based";
        String ip  = clientIp(req);
        String key = ip + ":" + bucketName;

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(defaultCap, defaultWin));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // Nice headers for clients/monitoring
        res.setHeader("X-RateLimit-Limit",     String.valueOf(defaultCap));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, probe.getRemainingTokens())));
        long resetSec = (long)Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0);
        res.setHeader("X-RateLimit-Reset",     String.valueOf(Math.max(0, resetSec)));

        if (!probe.isConsumed()) {
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(Math.max(1, resetSec)));
            res.setContentType("application/json");
            res.getWriter().write("""
              {"status":429,"error":"Too Many Requests","code":"RATE_LIMITED",
               "message":"Rate limit exceeded","bucket":"%s","ip":"%s","retryAfterSeconds":%d}
            """.formatted(bucketName, ip, Math.max(1, resetSec)));
            return;
        }

        chain.doFilter(req, res);
    }

    private Bucket newBucket(int capacity, int windowSeconds) {
        var limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofSeconds(windowSeconds)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    private static String bucketFor(String path, String method) {
        if ("POST".equalsIgnoreCase(method) && ("/login".equals(path) || "/api/auth/login".equals(path))) return "login";
        if (path.startsWith("/api/user/")) return "user";
        return "default";
    }

    // Simple IP resolver (first X-Forwarded-For if present, else remoteAddr)
    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String rip = req.getHeader("X-Real-IP");
        return (rip != null && !rip.isBlank()) ? rip.trim() : req.getRemoteAddr();
    }
}


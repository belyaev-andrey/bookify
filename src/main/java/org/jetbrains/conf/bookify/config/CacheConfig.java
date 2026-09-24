package org.jetbrains.conf.bookify.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Enables Spring's cache abstraction; the Caffeine cache manager itself is autoconfigured from {@code spring.cache.*}.
 * <p>
 * The order places the cache advice outside the transaction advice (which defaults to {@code LOWEST_PRECEDENCE}),
 * so puts and evictions happen after the method's transaction commits. Otherwise, a concurrent reader could re-cache
 * the old row between the eviction and the commit. This relies on each cached or evicting method being where its
 * transaction starts.
 */
@Configuration
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
class CacheConfig {
}

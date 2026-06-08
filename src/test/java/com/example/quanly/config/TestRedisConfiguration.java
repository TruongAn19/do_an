package com.example.quanly.config;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Profile "test" tắt RedisAutoConfiguration (xem application-test.properties) nên không có
 * {@link StringRedisTemplate} thật — nhưng {@code PendingBookingCache} lại bắt buộc cần nó,
 * khiến mọi {@code @SpringBootTest} không load được ApplicationContext.
 *
 * <p>Trước đây bean này là mock RỖNG (mọi op trả {@code null}) nên không test được luồng Redis.
 * Nay nâng cấp thành <b>fake in-memory CÓ TRẠNG THÁI</b> chống lưng bởi {@link ConcurrentHashMap}:
 * đủ để test idempotent của {@code PendingBookingCache.getAndRemove} (atomic GETDEL) và marker
 * kết quả {@code storeResult/getResult}. Các op được map đúng ngữ nghĩa Redis:
 * <ul>
 *   <li>{@code opsForValue().set(k,v,ttl)} → {@code map.put} (TTL bỏ qua trong test)</li>
 *   <li>{@code opsForValue().setIfAbsent(k,v,ttl)} → {@code map.putIfAbsent} trả TRUE nếu absent</li>
 *   <li>{@code opsForValue().get(k)} → {@code map.get}</li>
 *   <li>{@code opsForValue().getAndDelete(k)} → {@code map.remove} (atomic — đúng GETDEL)</li>
 *   <li>{@code delete(k)} → {@code map.remove}</li>
 * </ul>
 * Các test A1/A2/Security không đi qua Redis nên fake này không ảnh hưởng (hành xử như store rỗng).
 */
@Configuration
@Profile("test")
public class TestRedisConfiguration {

    @Bean
    @SuppressWarnings("unchecked")
    public StringRedisTemplate stringRedisTemplate() {
        ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(template.opsForValue()).thenReturn(valueOps);

        // set(k, v, Duration ttl) → put (đè giá trị)
        Mockito.doAnswer((Answer<Void>) inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(valueOps).set(Mockito.anyString(), Mockito.anyString(), Mockito.any(java.time.Duration.class));

        // setIfAbsent(k, v, Duration ttl) → putIfAbsent; TRUE nếu chưa tồn tại
        Mockito.when(valueOps.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.any(java.time.Duration.class)))
                .thenAnswer(inv -> store.putIfAbsent(inv.getArgument(0), inv.getArgument(1)) == null);

        // get(k)
        Mockito.when(valueOps.get(Mockito.anyString()))
                .thenAnswer(inv -> store.get(inv.getArgument(0)));

        // getAndDelete(k) → remove (atomic, đúng ngữ nghĩa GETDEL)
        Mockito.when(valueOps.getAndDelete(Mockito.anyString()))
                .thenAnswer(inv -> store.remove(inv.getArgument(0)));

        // delete(k)
        Mockito.when(template.delete(Mockito.anyString()))
                .thenAnswer(inv -> store.remove(inv.getArgument(0)) != null);

        return template;
    }
}

package com.example.quanly;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import com.example.quanly.exception.BusinessConflictException;
import com.example.quanly.repository.MatchParticipantRepository;
import com.example.quanly.repository.MatchPostRepository;
import com.example.quanly.repository.NotificationRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.MatchPostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MatchPostConcurrencyTest {

    @Autowired MatchPostService matchPostService;
    @Autowired MatchPostRepository matchPostRepository;
    @Autowired MatchParticipantRepository participantRepository;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;

    private User owner;
    private User userA;
    private User userB;
    private MatchPost post;

    @BeforeEach
    void seed() {
        owner = user("match-owner@example.com");
        userA = user("match-a@example.com");
        userB = user("match-b@example.com");

        post = new MatchPost();
        post.setUser(owner);
        post.setPlayDate(LocalDate.now().plusDays(1));
        post.setArea("District 1");
        post.setTimeSlot("19:00");
        post.setStatus("open");
        post.setMaxParticipants(1);
        post.setCurrentParticipants(0);
        post = matchPostRepository.save(post);
    }

    @AfterEach
    void cleanup() {
        notificationRepository.deleteAll();
        participantRepository.deleteAll();
        matchPostRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void twoConcurrentJoinsForLastSlot_onlyOneSucceeds() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Callable<String> joinA = () -> joinAfterBarrier(barrier, userA);
            Callable<String> joinB = () -> joinAfterBarrier(barrier, userB);
            Future<String> first = pool.submit(joinA);
            Future<String> second = pool.submit(joinB);
            List<String> results = List.of(first.get(), second.get());

            assertEquals(1, results.stream().filter("JOINED"::equals).count());
            assertEquals(1, results.stream().filter("FULL"::equals).count());
            assertEquals(1, participantRepository.countByMatchPostId(post.getId()));

            MatchPost after = matchPostRepository.findById(post.getId()).orElseThrow();
            assertEquals(1, after.getCurrentParticipants());
            assertEquals("full", after.getStatus());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void sameUserCannotJoinTwice() {
        matchPostService.joinPost(post.getId(), userA);

        assertThrows(BusinessConflictException.class,
                () -> matchPostService.joinPost(post.getId(), userA));
        assertEquals(1, participantRepository.countByMatchPostId(post.getId()));
    }

    private String joinAfterBarrier(CyclicBarrier barrier, User user) throws Exception {
        barrier.await();
        try {
            matchPostService.joinPost(post.getId(), user);
            return "JOINED";
        } catch (BusinessConflictException ex) {
            return "FULL";
        }
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("secret");
        user.setFullName("Match Tester");
        user.setPhone("0900000000");
        return userRepository.save(user);
    }
}

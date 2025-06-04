package com.example.quanly.repository;

import com.example.quanly.domain.PasswordResetToken;
import com.example.quanly.domain.User;
import com.example.quanly.domain.utility.PasswordResetTokenDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PasswordResetTokenDAOImpl implements PasswordResetTokenDAO {

    @PersistenceContext
    EntityManager em;

    @Override
    public PasswordResetToken findByToken(String token) {
        String jpql = "SELECT t FROM PasswordResetToken t WHERE t.token = :token";
        return em.createQuery(jpql, PasswordResetToken.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public PasswordResetToken findByUser(User user) {
        String jpql = "SELECT t FROM PasswordResetToken t WHERE t.user = :user";
        return em.createQuery(jpql, PasswordResetToken.class)
                .setParameter("user", user)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(PasswordResetToken token) {
        em.persist(token);
    }

    @Override
    public void delete(PasswordResetToken token) {
        em.remove(em.contains(token) ? token : em.merge(token));
    }

}

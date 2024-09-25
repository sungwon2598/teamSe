package com.seoil.team.oauthTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor

public class UserRepository {
    private final EntityManager em;

    public void save(User user){
        System.out.println("user = " + user);
        em.persist(user);
    }

    public User findByUsername(String username){
        System.out.println("username = " + username);
        return em.find(User.class, username); //jpa가 제공하는 find
    }
}

package com.example.trustwipe.repository;

import com.example.trustwipe.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing User data in MongoDB.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Find a user by their unique email address.
     * @param email The email to search for.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email.
     * @param email The email to check.
     * @return True if a user exists with the given email.
     */
    boolean existsByEmail(String email);
}

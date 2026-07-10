package com.lifeline.lifeline.repository;

import com.lifeline.lifeline.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);
}
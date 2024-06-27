package com.project1.project.repository;

import com.project1.project.model.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    @Query("{'client_id': ?0}")
    Optional<UserEntity> findByClientId(String client_id);
    @Query("{'mobile_no': ?0}")
    Optional<UserEntity> findByMobileNo(long mobileNo);


}
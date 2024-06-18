package com.project1.project.repository;


import com.project1.project.model.ClientDocument;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends MongoRepository<ClientDocument, UUID> {
    @Query("{'created_for.person_id': ?0}")
    List<ClientDocument> findByPersonId(int personId);

    @Query("{'file_information.application_transaction_id' :  ?0}")
    Optional<ClientDocument> findByApplicationTransactionId(long application_transaction_id);

}

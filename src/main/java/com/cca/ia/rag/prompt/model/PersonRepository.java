package com.cca.ia.rag.prompt.model;

import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<PersonEntity, Long> {

    PersonEntity findByUsernameAndActiveTrue(String username);

}

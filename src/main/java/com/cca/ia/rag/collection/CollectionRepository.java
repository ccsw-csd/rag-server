package com.cca.ia.rag.collection;

import org.springframework.data.repository.CrudRepository;

import com.cca.ia.rag.collection.model.CollectionEntity;

public interface CollectionRepository extends CrudRepository<CollectionEntity, Long> {

}
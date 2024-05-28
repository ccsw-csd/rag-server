package com.cca.ia.rag.collection.database;

import com.cca.ia.rag.collection.model.CollectionPropertyEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CollectionPropertyRepository extends CrudRepository<CollectionPropertyEntity, Long> {

    List<CollectionPropertyEntity> findByCollectionId(Long collectionId);

}

package com.cca.ia.rag.collection;

import java.util.List;

import com.cca.ia.rag.collection.model.CollectionDto;
import com.cca.ia.rag.collection.model.CollectionEntity;

public interface CollectionService {

    /**
     * Método para recuperar todos los collection
     *
     * @return {@link List} de {@link CollectionEntity}
     */
    List<CollectionEntity> findAll();

    CollectionEntity save(CollectionDto data);
}

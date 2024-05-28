package com.cca.ia.rag.collection;

import com.cca.ia.rag.collection.model.*;

import java.util.List;

public interface CollectionService {

    /**
     * Método para recuperar todos los collection
     *
     * @return {@link List} de {@link CollectionEntity}
     */
    List<CollectionEntity> findAll();

    /**
     * Método para recuperar un collection por su id
     * @param id
     * @return
     */
    CollectionEntity findById(Long id);

    void save(CollectionDto data);

    List<CollectionPropertyDto> findProperties(Long collectionId);

    List<CollectionPropertyEntity> findPrompts(Long collectionId);

    void saveProperties(Long collectionId, CollectionPropertyRequestDto data);
}

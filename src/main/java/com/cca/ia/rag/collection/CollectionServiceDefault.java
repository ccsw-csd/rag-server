package com.cca.ia.rag.collection;

import com.cca.ia.rag.collection.model.CollectionEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CollectionServiceDefault implements CollectionService {

    @Autowired
    CollectionRepository collectionRepository;

    @Override
    public List<CollectionEntity> findAll() {

        return (List<CollectionEntity>) this.collectionRepository.findAll();
    }

    @Override
    public CollectionEntity findById(Long id) {

        return this.collectionRepository.findById(id).orElse(null);
    }

    /*
    @Override
    @Transactional
    public CollectionEntity save(CollectionDto data) {

        CollectionEntity collection = new CollectionEntity();
        if (data.getId() != null) {
            collection.setId(data.getId());
        }
        collection.setDescription(data.getDescription());
        collection.setName(data.getName());
        return this.collectionRepository.save(collection);
    }

     */

}

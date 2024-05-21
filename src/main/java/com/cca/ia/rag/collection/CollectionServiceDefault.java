package com.cca.ia.rag.collection;

import com.cca.ia.rag.collection.model.CollectionDto;
import com.cca.ia.rag.collection.model.CollectionEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
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

    @Override
    @Transactional
    public void save(CollectionDto data) {

        CollectionEntity collection = null;
        if (data.getId() != null)
            collection = findById(data.getId());
        else
            collection = new CollectionEntity();

        BeanUtils.copyProperties(data, collection);

        this.collectionRepository.save(collection);
    }

}

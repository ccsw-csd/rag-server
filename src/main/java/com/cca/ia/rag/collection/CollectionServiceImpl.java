package com.cca.ia.rag.collection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cca.ia.rag.collection.model.CollectionEntity;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    CollectionRepository collectionRepository;

    @Override
    public List<CollectionEntity> findAll() {
        return (List<CollectionEntity>) this.collectionRepository.findAll();
    }

}
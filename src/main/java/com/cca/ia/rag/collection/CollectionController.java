package com.cca.ia.rag.collection;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cca.ia.rag.collection.model.CollectionDto;
import com.cca.ia.rag.collection.model.CollectionEntity;

@RequestMapping(value = "/collection")
@RestController
@CrossOrigin(origins = "*")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    ModelMapper mapper;

    /**
     * Método para recuperar todos los collection
     *
     * @return {@link List} de {@link CollectionDto}
     */

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public List<CollectionDto> findAll() {

        List<CollectionEntity> collections = this.collectionService.findAll();

        return collections.stream().map(e -> mapper.map(e, CollectionDto.class)).collect(Collectors.toList());

    }

}
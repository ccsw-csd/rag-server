package com.cca.ia.rag.collection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cca.ia.rag.collection.model.CollectionDto;
import com.cca.ia.rag.config.mapper.BeanMapper;

@RequestMapping(value = "/collection")
@RestController
@CrossOrigin(origins = "*")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private BeanMapper beanMapper;

    /**
     * Método para recuperar todos los collection
     *
     * @return {@link List} de {@link CollectionDto}
     */

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public List<CollectionDto> findAll() {

        return this.beanMapper.mapList(this.collectionService.findAll(), CollectionDto.class);
    }

}
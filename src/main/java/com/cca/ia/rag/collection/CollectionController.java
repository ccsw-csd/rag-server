package com.cca.ia.rag.collection;

import com.cca.ia.rag.collection.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/collection")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ModelMapper mapper;

    /**
     * Método para recuperar todos los collection
     *
     * @return {@link List} de {@link CollectionDto}
     */

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping(path = "")
    public List<CollectionDto> findAll() {

        List<CollectionEntity> collections = this.collectionService.findAll();

        return collections.stream().map(e -> mapper.map(e, CollectionDto.class)).collect(Collectors.toList());

    }

    /**
     * @param data
     * @return
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "")
    public void save(@RequestBody CollectionDto data) {

        this.collectionService.save(data);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(path = "/{collectionId}/properties")
    public List<CollectionPropertyDto> findProperties(@PathVariable(value = "collectionId") Long collectionId) {

        return this.collectionService.findProperties(collectionId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(path = "/{collectionId}/prompts")
    public List<CollectionPropertyDto> findPrompts(@PathVariable(value = "collectionId") Long collectionId) {

        List<CollectionPropertyEntity> properties = this.collectionService.findPrompts(collectionId);
        return properties.stream().map(e -> mapper.map(e, CollectionPropertyDto.class)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/{collectionId}/properties")
    public void saveProperties(@PathVariable(value = "collectionId") Long collectionId, @RequestBody CollectionPropertyRequestDto data) {

        this.collectionService.saveProperties(collectionId, data);
    }

}
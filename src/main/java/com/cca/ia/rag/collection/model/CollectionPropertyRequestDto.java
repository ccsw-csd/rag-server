package com.cca.ia.rag.collection.model;

import java.util.List;

public class CollectionPropertyRequestDto {

    private List<CollectionPropertyDto> properties;

    public List<CollectionPropertyDto> getProperties() {
        return properties;
    }

    public void setProperties(List<CollectionPropertyDto> properties) {
        this.properties = properties;
    }
}

package com.cca.ia.rag.collection;

import com.cca.ia.rag.collection.database.CollectionPropertyRepository;
import com.cca.ia.rag.collection.database.CollectionRepository;
import com.cca.ia.rag.collection.model.*;
import jakarta.transaction.Transactional;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionServiceDefault implements CollectionService {

    @Value("${cipher.key}")
    private String cipherKey;

    @Value("classpath:/prompts/document-assistant.st")
    private Resource documentSystemPromptResource;

    @Value("classpath:/prompts/query-assistant.st")
    private Resource sqlSystemPromptResource;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private CollectionPropertyRepository collectionPropertiesRepository;

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

        if (data.getId() == null) {
            createDefaultConfiguration(collection);
        }
    }

    @Override
    public List<CollectionPropertyDto> findProperties(Long collectionId) {

        List<CollectionPropertyEntity> propertyList = this.collectionPropertiesRepository.findByCollectionId(collectionId);
        List<CollectionPropertyDto> properties = new ArrayList<>();

        for (CollectionPropertyEntity property : propertyList) {

            if (property.getKey().toLowerCase().contains("prompt") == true)
                continue;

            CollectionPropertyDto dto = new CollectionPropertyDto();
            BeanUtils.copyProperties(property, dto);
            properties.add(dto);

            if (dto.getKey().equals("apiKey") || dto.getKey().equals("databasePassword")) {
                try {
                    dto.setValue(decrypt(dto.getValue()));
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return properties;
    }

    @Override
    public List<CollectionPropertyEntity> findPrompts(Long collectionId) {

        List<CollectionPropertyEntity> properties = this.collectionPropertiesRepository.findByCollectionId(collectionId);
        return properties.stream().filter(p -> p.getKey().toLowerCase().contains("prompt") == true).collect(Collectors.toList());
    }

    @Override
    public void saveProperties(Long collectionId, CollectionPropertyRequestDto data) {

        CollectionEntity collection = findById(collectionId);
        List<CollectionPropertyEntity> properties = this.collectionPropertiesRepository.findByCollectionId(collectionId);

        for (CollectionPropertyDto property : data.getProperties()) {
            CollectionPropertyEntity entity = properties.stream().filter(p -> p.getKey().equals(property.getKey())).findFirst().orElse(null);
            if (entity == null) {
                entity = new CollectionPropertyEntity();
                entity.setCollection(collection);
                entity.setKey(property.getKey());
            }

            if (property.getKey().equals("apiKey") || property.getKey().equals("databasePassword")) {
                try {
                    property.setValue(encrypt(property.getValue()));
                } catch (Exception e) {
                    continue;
                }
            }

            entity.setValue(property.getValue());
            properties.add(entity);
        }

        collectionPropertiesRepository.saveAll(properties);
    }

    private void createDefaultConfiguration(CollectionEntity collection) {

        List<CollectionPropertyEntity> config = new ArrayList<>();

        config.add(createProperty(collection, "documentSystemPrompt", documentSystemPromptResource));
        config.add(createProperty(collection, "sqlSystemPrompt", sqlSystemPromptResource));
        config.add(createProperty(collection, "llm", "gpt-3.5-turbo"));
        config.add(createProperty(collection, "context-ratio", "60"));
        config.add(createProperty(collection, "noAutocontext", "false"));
        config.add(createProperty(collection, "onlyDoc", "false"));
        config.add(createProperty(collection, "onlyCode", "false"));
        config.add(createProperty(collection, "query", "false"));

        collectionPropertiesRepository.saveAll(config);
    }

    private CollectionPropertyEntity createProperty(CollectionEntity collection, String key, Resource resource) {
        try {
            return createProperty(collection, key, resource.getContentAsString(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CollectionPropertyEntity createProperty(CollectionEntity collection, String key, String value) {
        CollectionPropertyEntity property = new CollectionPropertyEntity();
        property.setCollection(collection);
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    private String encrypt(String text) throws Exception {

        Key key = new SecretKeySpec(cipherKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(text.getBytes());

        return Base64.encodeBase64String(encrypted);
    }

    private String decrypt(String text) throws Exception {
        Key key = new SecretKeySpec(cipherKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] encrypted = Base64.decodeBase64(text);
        byte[] original = cipher.doFinal(encrypted);
        return new String(original);
    }

}

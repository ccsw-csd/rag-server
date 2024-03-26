package com.cca.ia.rag.utils;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author ccsw
 *
 */
@Service
public class UtilsServiceDefault implements UtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(UtilsServiceDefault.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {

        try {
            return new Manifest(UtilsServiceDefault.class.getResourceAsStream("/META-INF/MANIFEST.MF")).getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION).toString();
        } catch (Exception e) {
            LOG.error("Error al extraer la version");
        }

        return "?";
    }

}

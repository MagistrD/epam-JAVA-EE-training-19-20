package com.epam.training.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CatalogAndUserGeneratorStarter implements GeneratorStarter {
    private final CatalogGenerator catalogGenerator;

    @Autowired
    public CatalogAndUserGeneratorStarter(CatalogGenerator catalogGenerator) {
        this.catalogGenerator = catalogGenerator;
    }

    @Override
    public void generate() {
        catalogGenerator.catalog();
    }
}

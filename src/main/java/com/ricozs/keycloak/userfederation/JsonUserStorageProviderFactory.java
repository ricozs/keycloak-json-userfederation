package com.ricozs.keycloak.userfederation;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

public class JsonUserStorageProviderFactory implements UserStorageProviderFactory<JsonUserStorageProvider> {


    @Override
    public String getId() {
        return "json-userstorage";
    }

    @Override
    public JsonUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        System.out.println("####createstorageprovider####");
        return new JsonUserStorageProvider(session, model, new JsonUserRepository());
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        System.out.println("####getConfigProperties####");
        return UserStorageProviderFactory.super.getConfigProperties();
    }
}

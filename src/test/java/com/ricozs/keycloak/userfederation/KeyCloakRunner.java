package com.ricozs.keycloak.userfederation;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserStorageProviderResource;
import org.keycloak.representations.idm.ComponentTypeRepresentation;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import java.util.List;

class KeyCloakRunner {

    private static final String MASTER_REALM = "master";
    private static final String ADMIN_CLI = "admin-cli";


    public static void main(String[] args) throws Exception {
        try (KeycloakDebugContainer keycloak = new KeycloakDebugContainer("quay.io/keycloak/keycloak:21.1.1", 9000)) {

            //Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);

            keycloak.setExposedHttpPort(8080);
            keycloak.withRealmImportFile("test-realm.json");
            keycloak.withProviderClassesFrom("target/classes");
            keycloak.start();
            keycloak.followOutput(new ToStringConsumer(), OutputFrame.OutputType.STDOUT);
            Keycloak keycloakClient = Keycloak.getInstance(
                    keycloak.getAuthServerUrl(), MASTER_REALM,
                    keycloak.getAdminUsername(), keycloak.getAdminPassword(), ADMIN_CLI);

            RealmResource realm = keycloakClient.realm(MASTER_REALM);
            UserStorageProviderResource userStorageProviderResource = realm.userStorage();

            List<ComponentTypeRepresentation> componentTypeRepresentations = keycloakClient.serverInfo()
                    .getInfo().getComponentTypes().get("org.keycloak.storage.UserStorageProvider");

            System.out.println("Keycloak Running, you can now attach your remote debugger to port 9000!");
            System.in.read();

        }
    }

}
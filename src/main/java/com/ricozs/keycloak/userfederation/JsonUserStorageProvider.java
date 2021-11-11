package com.ricozs.keycloak.userfederation;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.ReadOnlyUserModelDelegate;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

public class JsonUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel componentModel;
    private final JsonUserRepository userRepository;

    public JsonUserStorageProvider(KeycloakSession keycloakSession, ComponentModel componentModel,
                                   JsonUserRepository userRepository) {
        this.session = keycloakSession;
        this.componentModel = componentModel;
        this.userRepository = userRepository;
    }

    @Override
    public void close() {
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        StorageId storageId = new StorageId(id);
        return getUserByUsername(storageId.getExternalId(), realm);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        User user = userRepository.findUserByUsername(username);
        if(user == null) {
            return null;
        }
        return importUserFromJson(session, realm, user);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        return null;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel) || !supportsCredentialType(credentialInput.getType())) {
            return false;
        }
        return userRepository.validateCredentials(user.getUsername(), credentialInput.getChallengeResponse());
    }


    private UserModel importUserFromJson(KeycloakSession session, RealmModel realm, User user) {
        UserModel imported = null;

        UserModel existingLocalUser = session.userLocalStorage().getUserByUsername(realm, user.getUsername());
        if(existingLocalUser != null) {
            imported = existingLocalUser;

            if (session.userCache() != null) {
                session.userCache().evict(realm, existingLocalUser);
            }
        }
        else {
            imported = session.userLocalStorage().addUser(realm, user.getUsername());
            imported.setFederationLink(componentModel.getId());
        }
        //always enable
        imported.setEnabled(true);
        imported.setFirstName(user.getFirstName());
        imported.setLastName(user.getLastName());

        return new ReadOnlyUserModelDelegate(imported);
    }
}

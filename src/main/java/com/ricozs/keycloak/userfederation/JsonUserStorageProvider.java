package com.ricozs.keycloak.userfederation;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.ReadOnlyUserModelDelegate;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageUtil;
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
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        return getUserByUsername(realm, storageId.getExternalId());
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        User user = userRepository.findUserByUsername(username);
        if(user == null) {
            return null;
        }
        return importUserFromJson(session, realm, user);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
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

        UserModel existingLocalUser = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(realm, user.getUsername());
        if(existingLocalUser != null) {
            imported = existingLocalUser;

            if (UserStorageUtil.userCache(session) != null) {
                UserStorageUtil.userCache(session).evict(realm, existingLocalUser);
            }
        }
        else {
            imported = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, user.getUsername());
            imported.setFederationLink(componentModel.getId());
        }
        //always enable
        imported.setEnabled(true);
        imported.setFirstName(user.getFirstName());
        imported.setLastName(user.getLastName());

        return new ReadOnlyUserModelDelegate(imported);
    }
}

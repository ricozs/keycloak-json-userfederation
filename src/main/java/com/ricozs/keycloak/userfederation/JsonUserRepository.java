package com.ricozs.keycloak.userfederation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JsonUserRepository {

    private final ObjectMapper objectMapper;
    private static final String FILENAME = "users.json";


    public JsonUserRepository() {
        this.objectMapper = new ObjectMapper();
    }

    public User findUserByUsername(String username) {
        UserData userData = loadData();
        return userData.getUsers().stream().filter(e -> e.getUsername().equals(username))
                .map(e-> {
                    User user = new User();
                    user.setUsername(e.getUsername());
                    user.setFirstName(e.getFirstname());
                    user.setLastName(e.getLastname());
                    return user;
                }).findFirst().orElse(null);
    }

    public boolean validateCredentials(String username, String credentials) {
        UserData userData = loadData();
        return userData.getUsers().stream()
                .filter(e -> e.getUsername().equals(username))
                .anyMatch(e-> e.getPassword().equals(credentials));
    }

    private UserData loadData() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(FILENAME)){
            return objectMapper.readValue(is, UserData.class);
        }catch (IOException ioe) {
            throw new IllegalStateException("failed to load userinfo:" + ioe.getMessage(), ioe);
        }
    }

    public static class UserData {

        private List<UserEntry> users;

        public List<UserEntry> getUsers() {
            return users;
        }

        public void setUsers(List<UserEntry> users) {
            this.users = users;
        }

        public static class UserEntry {
            private String username;
            private String firstname;
            private String lastname;
            private String password;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getFirstname() {
                return firstname;
            }

            public void setFirstname(String firstname) {
                this.firstname = firstname;
            }

            public String getLastname() {
                return lastname;
            }

            public void setLastname(String lastname) {
                this.lastname = lastname;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }
    }

}

package com.ricozs.keycloak.userfederation;

import dasniko.testcontainers.keycloak.KeycloakContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeycloakDebugContainer extends KeycloakContainer {

    private static final int CONTAINER_HTTP_PORT = 8080;

    private final int debugPort;
    private Integer exposedHttpPort;

    public KeycloakDebugContainer(String image, int debugPort) {
        super(image);
        this.debugPort = debugPort;
    }

    public void setExposedHttpPort(Integer exposedHttpPort) {
        this.exposedHttpPort = exposedHttpPort;
    }

    @Override
    protected void configure() {
        super.configure();
        List<Integer> ports = new ArrayList<>(getExposedPorts());
        ports.add(debugPort);
        this.withExposedPorts(ports.toArray(new Integer[]{}));

        List<String> extendedCommand = new ArrayList<>(Arrays.asList(this.getCommandParts()));
        extendedCommand.add("--debug *:" + debugPort);
        super.addFixedExposedPort(debugPort,debugPort);

        if(exposedHttpPort != null) {
            addFixedExposedPort(exposedHttpPort, CONTAINER_HTTP_PORT);
        }

        this.withCommand(extendedCommand.toArray(new String[]{}));
    }

}

package com.mitmws.environment;

import java.io.Serializable;
import java.util.ArrayList;

public class EnvironmentVariableSet implements Serializable {
    private ArrayList<EnvironmentVariable> environmentVariables = new ArrayList<>();

    public EnvironmentVariableSet() {

    }

    public ArrayList<EnvironmentVariable> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(ArrayList<EnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public void addVariables( ArrayList<EnvironmentVariable> newVars ) {
        environmentVariables.addAll(newVars);
    }
}

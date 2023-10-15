package com.example.project2project2team16.utils;

public class AppConfig {
    private String inputFilePath;
    private int numProcessors;
    private int numCores;
    private boolean visualize;
    private String outputFileName;

    public AppConfig(int numCores) {
        this.numCores = numCores;
    }

    public AppConfig(String inputFilePath, int numProcessors, int numCores, boolean visualize, String outputFileName) {
        this.inputFilePath = inputFilePath;
        this.numProcessors = numProcessors;
        this.numCores = numCores;
        this.visualize = visualize;
        this.outputFileName = outputFileName;
    }

    public String getInputFilePath() {
        return this.inputFilePath;
    }

    public int getNumProcessors() {
        return this.numProcessors;
    }

    public int getNumCores() {
        return this.numCores;
    }

    public boolean isVisualized() {
        return this.visualize;
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }
}

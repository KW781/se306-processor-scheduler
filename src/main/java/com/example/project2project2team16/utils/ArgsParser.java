package com.example.project2project2team16.utils;

import com.example.project2project2team16.exceptions.InvalidArgsException;

import java.io.File;

public class ArgsParser {
    public static AppConfig parseArgs(String[] args) {
        String inputFilePath = null;
        int numProcessors = 1;
        try {
            inputFilePath = args[0];
            numProcessors = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Invalid arguments. Please provide a valid input file and number of processors.");
            throw new InvalidArgsException();
        }

        int numCores = 1; // Default number of cores

        boolean visualize = false;
        String outputFileName = "INPUT-output.dot"; // Default output file name

        // Parse command-line arguments
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                // processes the number of cores, if provided in the command line
                case "-p":
                    // checks whether an actual number of cores is provided after '-p', if not then an appropriate error is thrown
                    if (i + 1 < args.length) {
                        try {
                            numCores = Integer.parseInt(args[i + 1]);
                            i++; // Skip the next argument since it's been used
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid value for -p. Please provide an integer.");
                            throw new InvalidArgsException();
                        }
                    } else {
                        System.err.println("Please provide a number of cores when using -p");
                        throw new InvalidArgsException();
                    }
                    break;
                // if -v is provided in the command line, then we need to show the visualisation
                case "-v":
                    visualize = true;
                    break;
                // process the output file name if one is provided in the command line
                case "-o":
                    if (i + 1 < args.length) {
                        outputFileName = args[i + 1];
                        i++; // Skip the next argument since it's been used
                    } else {
                        System.err.println("Please provide an output file name when using -o");
                        throw new InvalidArgsException();
                    }
                    break;
                default:
                    System.err.println("Unknown argument provided");
                    throw new InvalidArgsException();
            }
        }

        // test that the input file path provided is a valid one
        File testFile = new File(inputFilePath);
        if (!testFile.exists()) {
            System.err.println("System could not find the input file provided. Please provide a valid file path/name.");
            throw new InvalidArgsException();
        }

        return new AppConfig(inputFilePath, numProcessors, numCores, visualize, outputFileName);
    }
}

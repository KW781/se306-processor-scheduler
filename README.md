# Project Description
This project is part of the SOFTENG306 design course at the University of Auckland and uses the fundementals of artificial intelligence and parallel processing power to solve a difficult scheduling problem. 

Our code stems off developing a simple branch-and-bound type algorithm that solves a scheduling problem given from a hypothetical *client*. Following this initial principle, the goal of this project aims to achieve the highest possible performance through parallelisation and visualise this process. 

## 👋 About Us
We are **Parallels** (Team 16), a group of software engineering students at the [University of Auckland](https://www.auckland.ac.nz) aiming to develop and implement a maintainable and high-performance software algorithm.

You can find out more about us [here](https://github.com/UOASOFTENG306/project-2-project-2-team-16/wiki/Contributors)

## 📝 Running The Project

The project is run as a JAR file. Download the `scheduler.jar` file from either the "Milestone 1" or "Final" release depending on which one you want to run. Then in the directory where your JAR file is stored, run the JAR from the terminal like so:

```
java -jar scheduler.jar COMMAND_LINE_PARAMS
```
Note that `COMMAND_LINE_PARAMS` is a series of command line parameters that specify the configuration for the app to be run with:
* `INPUT.dot` - The input dot file that you would like the search to be performed on, `INPUT` is the file name (Compulsory).
* `NUM_PROCESSORS` - The number of processors that the tasks in the task graph need to be scheduled on (Compulsory).
* `-p NUM_CORES` - This flag specifies the number of cores that the searching needs to be performed with, `NUM-CORES` is the number of cores. The default number of cores is 1 (Optional).
* `-o OUTPUT.dot` - The name of the output dot file for the optimal schedule that you would like it to be, `OUTPUT` is the file name. The default file name is `INPUT-output.dot` where `INPUT` is the file name of the input dot file (Optional).
* `-v` - If this flag is added, then the app runs with the visualisation, otherwise it doesn't (Optional).

For example, let's suppose I wanted to run the app with the following conditions:
* The input file name is `input_graph.dot`
* The number of processors that the tasks should be scheduled on is 2
* The scheduling should be performed with 2 cores
* The name of the output file should be `output_graph.dot`
* The visualisation should be run

Then I would run the following in the terminal:
```
java -jar scheduler.jar input_graph.dot 2 -p 2 -o output_graph -v
```


## 💾 Technology Stack

<div align="center" class="row">

![image](https://img.shields.io/badge/OpenJDK-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![image](https://img.shields.io/badge/apache_maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![image](https://img.shields.io/badge/Junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

</div>

This project uses:
- [Java](https://www.java.com/en/) as the primary language
- [JavaFX](https://openjfx.io/) and vanilla [CSS](https://www.w3.org/Style/CSS) for the interface and styling
- [JUnit](https://junit.org/junit5/) for testing and [Maven](https://maven.apache.org/) for project handling

This project was developed and tested with [Java](https://www.java.com/en/)&nbsp;17+ and is compatible to run on Windows & Linux

## 🌟 Acknowledgements
- The [developers, testers and contributors](https://github.com/UOASOFTENG306/project-2-project-2-team-16/wiki/Contributors) who have participated in the development of this project.
- The course instructors for [SOFTENG&nbsp;306](https://courseoutline.auckland.ac.nz/dco/course/SOFTENG/306/1235) *Software Engineering Design 2*




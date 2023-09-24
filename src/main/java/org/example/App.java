package org.example;

import Model.DotFileParser;
import Model.Edge;
import Model.Graph;
import Model.Node;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args )
    {
        Graph graph = DotFileParser.manualParse("C:\\Users\\bluey\\OneDrive\\Desktop\\TestDotFile.dot");
        for (Node node : graph.getNodes().values()) {
            System.out.println("Node " + node.getId() + " has weight: " + node.getWeight());
        }

        for (Edge edge : graph.getEdges().values()) {
            System.out.println("Edge " + edge.getSource() + "->" + edge.getTarget() + " has weight: " + edge.getWeight());
        }
    }
}

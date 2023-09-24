package Model;

public class Edge {
    String source;
    String target;
    double weight;

    public Edge(String source, String target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }
}

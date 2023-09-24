package Model;

public class Node {
    String id;
    double weight;

    public Node(String id, double weight) {
        this.id = id;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }
}

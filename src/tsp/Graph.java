package tsp;

import java.util.ArrayList;
import java.util.Random;

public class Graph { // weighted complete graph
    static class Weight implements Comparable<Weight> {
        public int u,v;
        public double weight;

        public Weight(int u, int v, double weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public int compareTo(Weight other) {
            if (this.weight > other.weight)
                return 1;
            else if (this.weight < other.weight)
                return -1;
            return 0;
        }
    }

    static class Edge {
        public int u,v;
        public Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    public int n;
    public Weight[] weights;
    public double[][] weightMap;
    public ArrayList<Edge> edges;
    public double[][] points;

    public Graph(int n, Weight[] weights, ArrayList<Edge> edges, double[][] points) {
        this.n = n;
        this.weights = weights;
        this.edges = edges;
        this.points = points;
        assert(weights.length == n*n);

        // resolve weight map
        weightMap = new double[n][n];
        for(Weight w : weights) {
            int u = w.u;
            int v = w.v;
            weightMap[u][v] = w.weight;
            weightMap[v][u] = w.weight;
        }
    }

    public Graph(Graph other) {
        this.n = other.n;
        this.weights = new Weight[n*n];
        this.edges = new ArrayList<Edge>();
        this.points = other.points.clone();

        for(int i = 0; i < n*n; i++) {
            Weight w = other.weights[i];
            this.weights[i] = new Weight(w.u, w.v, w.weight);
        }
        for(int i = 0; i < other.edges.size(); i++) {
            Edge e = other.edges.get(i);
            this.edges.add(new Edge(e.u, e.v));
        }

        this.weightMap = new double[n][n];
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                this.weightMap[i][j] = other.weightMap[i][j];
    }

    public static Graph randomCompleteGraph(int n, double maxFromOrigin) {
        double[][] points = new double[n][2];

        Random rand = new Random();
        for(int i = 0; i < n; i++) {
            points[i][0] = (rand.nextDouble()*2.0-1.0) * maxFromOrigin;
            points[i][1] = (rand.nextDouble()*2.0-1.0) * maxFromOrigin;
        }

        double[][] weights = new double[n][n];
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                weights[i][j] = Math.sqrt(Math.pow(points[i][0] - points[j][0],2)
                        + Math.pow(points[i][1] - points[j][1],2));

        Weight[] w = new Weight[n*n];
        for(int i = 0; i < n*n; i++) {
            int u = i / n;
            int v = i % n;
            w[i] = new Weight(u, v, weights[u][v]);
        }

        ArrayList<Edge> edges = new ArrayList<Edge>();
        edges.ensureCapacity(n*n);
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                edges.add(new Edge(i, j));

        return new Graph(n, w, edges, points);
    }

    public String toDesmos() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n; i++) {
            double x = points[i][0];
            double y = points[i][1];
            sb.append("(" + x + "," + y + ")\n");
        }

        for(int i = 0; i < edges.size(); i++) {
            int u = edges.get(i).u;
            int v = edges.get(i).v;

            double x0 = points[u][0];
            double y0 = points[u][1];
            double x1 = points[v][0];
            double y1 = points[v][1];
            if(x1 == x0)
                continue;

            double m = (y1 - y0) / (x1 - x0);

            double xmin = Math.min(x0, x1);
            double xmax = Math.max(x0, x1);

            sb.append("y-"+y0+"="+m+"(x-"+x0+") \\left\\{"+xmin+"<=x<="+xmax+"\\right\\}\n");
        }

        return sb.toString();
    }

}

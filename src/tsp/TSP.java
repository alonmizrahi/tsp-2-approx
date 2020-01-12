package tsp;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TSP {

    private static int N_START = 4;
    private static int N_END = 11;
    private static int NUM_TESTS = 16;
    private static double MAX_FROM_ORIGIN = 100.0;

    private static double weight(ArrayList<Integer> tour, Graph g) {
        double res = 0.0;

        for(int i = 0; i < tour.size()-1; i++) {
            int cur = tour.get(i);
            int next = tour.get(i+1);

            res += g.weightMap[cur][next];
        }

        return res;
    }

    private static double minHamiltonCycleWeight(Graph g) {
        double res = Double.MAX_VALUE;
        ArrayList<Integer> finalCycle = new ArrayList<Integer>();
        int n = g.n;

        Integer[] vertices = new Integer[n];
        for(int i = 0; i < n; i++)
            vertices[i] = i;

        Permutator<Integer> permutator = new Permutator<Integer>(vertices);
        ArrayList<Integer> cycle = new ArrayList<Integer>();
        while(permutator.hasNext()) {
            Integer[] perm = permutator.next();

            cycle.clear();
            for(int i = 0; i < n; i++)
                cycle.add(perm[i]);
            cycle.add(perm[0]);

            double w = weight(cycle, g);
            if(w < res) {
                res = w;
                finalCycle.clear();
                finalCycle.addAll(cycle);
            }
        }

        //System.out.println("final cycle: " + finalCycle);
        return res;
    }

    static class ThreadTask implements Runnable {
        private double[] results;
        private int n;
        private int numTest;

        public ThreadTask(int n, int numTest, double[] results) {
            this.n = n;
            this.numTest = numTest;
            this.results = results;
        }

        public void run() {
            System.out.println("n = " + n + " : test num " + (numTest+1) + "/" + NUM_TESTS);
            // start with a random graph
            Graph g = Graph.randomCompleteGraph(n, MAX_FROM_ORIGIN);

            // calculate 2 approximation
            Graph mst = MST.kruskal(g);
            ArrayList<Integer> dfs = DFS.dfs(mst);
            ArrayList<Integer> shortcut = DFS.shortcut(dfs);
            double twoApproxWeight = weight(shortcut, g);

            // run brute force algorithm
            double optWeight = minHamiltonCycleWeight(g);

            double ratio = twoApproxWeight / optWeight;

            synchronized (results) {
                results[n-N_START] += (ratio / NUM_TESTS);
            }
        }
    }

    public static void main(String[] args) {
        double[] results = new double[N_END - N_START + 1];

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        for(int n = N_START; n <= N_END; n++) {
            long start = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(availableProcessors);
            for(int i = 0; i < NUM_TESTS; i++)
                executor.execute(new ThreadTask(n, i, results));

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            long end = System.currentTimeMillis();
            System.out.println("done! took " + (end - start) + " ms.");
            System.out.println("result for n=" + n + " : " + results[n-N_START]);
        }

        for(int i = 0; i < results.length; i++) {
            System.out.println("n=" + (i+N_START) + " : " + results[i]);
        }
    }
}

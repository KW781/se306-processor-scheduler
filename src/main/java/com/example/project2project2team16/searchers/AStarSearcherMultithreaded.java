package com.example.project2project2team16.searchers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A* searcher that can work with an input amount of threads.
 */
public class AStarSearcherMultithreaded extends GreedySearcher {
    Set<ScheduleNode> createdSchedules = ConcurrentHashMap.newKeySet();
    ScheduleNode currentOptimal = null;
    Object lock = new Object();
    List<PriorityBlockingQueue<ScheduleNode>> frontiers = new ArrayList<>();
    AtomicInteger doneThreads = new AtomicInteger();
    int tasksVisited = 0;
    Object visLock = new Object();


    public AStarSearcherMultithreaded(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void initialiseSearcher() {
        ScheduleNode startNode = problem.getStartNode();
        SchedulingProblem.initialiseF(startNode);

        super.initialiseSearcher();

        GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
        helper.addNode(startNode, startNode.getParent());
        helper.setStartNode(startNode);
    }

    /**
     * Adds new nodes to the frontier that meet requirements as do the parent class versions but takes a specific frontier to add to.
     *
     * @param frontier the frontier to add to
     * @param newNodes list of newly generated nodes to add to the frontier
     */
    protected void addToFrontier(PriorityBlockingQueue<ScheduleNode> frontier, List<ScheduleNode> newNodes) {
        for (ScheduleNode newNode : newNodes) {
            if (createdSchedules.contains(newNode)) {
                continue;
            }

            if (!newNode.isHadFixedTaskOrder()) {
                if (newNode.isEquivalent()) {
                    continue;
                }
            }

            frontier.add(newNode);
            createdSchedules.add(newNode);
        }
    }


    /**
     * Gets the node from the front of the provided frontier.
     *
     * @param frontier
     * @return Node at the front of the frontier.
     *         Returns null if the node has no possibility of improving on the current optimal schedule.
     */
    private ScheduleNode getNextNode(PriorityBlockingQueue<ScheduleNode> frontier) {
        ScheduleNode node = frontier.poll();

        if (node == null || currentOptimal != null && node.getfValue() >= currentOptimal.getValue()) {
            return null;
        }

        return node;
    }

    @Override
    public ScheduleNode search() {
        // Gets thread count from configuration
        Integer threadCount = VisualisationApplication.getThreadCount();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            frontiers.add(new PriorityBlockingQueue(10000, new ScheduleNodeAStarComparator(problem)));
        }

        addToFrontier(frontiers.get(0), Collections.singletonList(problem.getStartNode()));

        // Initial search to get starting nodes for all the threads
        ScheduleNode initialSearchResult = initialSearch(threadCount);

        if (initialSearchResult != null) {
            return initialSearchResult;
        }

        // Distributes starting nodes
        distributeFrontiers(threadCount);

        // Starts search threads
        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i;

            threads.add(
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadSearchPaper(threadIndex);
                        }
                    }));

            threads.get(i).start();
        }

        // Waits for all search threads to complete
        for (int i = 0; i < threadCount; i++) {
            try {
                threads.get(i).join();
            }
            catch (InterruptedException e) {
                System.out.println("Thread waiting failed");
                System.out.println(e.toString());
            }
        }

        return currentOptimal;
    }

    /**
     * Single worker search algorithm influenced by Oliver's research paper.
     * Ends when all workers have run out of viable nodes to expand.
     *
     * @param threadIndex index of this specific thread
     */
    private void threadSearchPaper(Integer threadIndex) {
        boolean hasWork = true;
        PriorityBlockingQueue<ScheduleNode> frontier = frontiers.get(threadIndex);

        List<Integer> otherThreads = IntStream.rangeClosed(0, frontiers.size() - 1).boxed().collect(Collectors.toList());

        otherThreads.remove(threadIndex);

        Random rand = new Random();


        // While not all threads are done
        while (doneThreads.get() < frontiers.size()) {
            while (!frontier.isEmpty()) {
                ScheduleNode nextNode = frontier.poll();

                // Delayed Sync possible

                if (nextNode == null) {
                    continue;
                }

                // Visualisation update
                if (nextNode.getVisited().size() > tasksVisited) {
                    synchronized (visLock) {
                        if (nextNode.getVisited().size() > tasksVisited) {
                            nextNode.setThreadId(threadIndex);
                            GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
                            helper.addNode(nextNode, nextNode.getParent());
                            helper.updateOptimalNode(nextNode);
                            tasksVisited = nextNode.getVisited().size();
                        }
                    }
                }

                // If node could be an improvement on the current optimal
                if (currentOptimal == null || nextNode.getfValue() < currentOptimal.getValue()) {
                    if (problem.isGoal(nextNode)) {
                        synchronized (lock) {
                            if (currentOptimal == null || nextNode.getValue() < currentOptimal.getValue()) {
                                currentOptimal = nextNode;
                            }
                        }
                    }
                    else {
                        addToFrontier(frontier, problem.getNeighbourStates(nextNode));
                        if (nextNode.hasUnpromisingChildren()) {
                            frontier.add(nextNode);
                            nextNode.setUnpromisingChildren(false);
                        }
                    }
                }
                else {
                    frontier.clear();
                }
            }

            // If just had work
            if (hasWork) {
                doneThreads.incrementAndGet();
                hasWork = false;
            }

            // Steal work
            if (frontiers.size() != 1) {
                Integer victim = otherThreads.get(rand.nextInt(frontiers.size() - 1));

                ScheduleNode stolen = frontiers.get(victim).poll();

                if (stolen != null && (currentOptimal == null || stolen.getfValue() < currentOptimal.getValue())) {
                    frontier.add(stolen);
                    hasWork = true;
                    doneThreads.decrementAndGet();
                }
            }
        }
    }

    /**
     * Distributes nodes between the different thread frontiers
     *
     * @param threadCount number of threads
     */
    private void distributeFrontiers(Integer threadCount) {
        PriorityBlockingQueue<ScheduleNode> nodes = frontiers.get(0);

        for (int i = 1; i < threadCount; i++) {
            frontiers.get(i).add(nodes.poll());
        }
    }

    /**
     * Run initial search for getting starting nodes for all the threads.
     *
     * @param threadCount number of threads
     * @return returns optimal schedule if found, null if not
     */
    public ScheduleNode initialSearch(Integer threadCount) {
        PriorityBlockingQueue<ScheduleNode> frontier = frontiers.get(0);

        while (!frontier.isEmpty()) {
            // If got enough nodes for the threads
            if (frontier.size() >= threadCount) {
                return null;
            }

            ScheduleNode nextNode = getNextNode(frontier);

            if (problem.isGoal(nextNode)) {
                return nextNode;
            } else {
                addToFrontier(frontier, problem.getNeighbourStates(nextNode));
                if (nextNode.hasUnpromisingChildren()) {
                    frontier.add(nextNode);
                    nextNode.setUnpromisingChildren(false);
                }
            }
        }

        return null;
    }
}
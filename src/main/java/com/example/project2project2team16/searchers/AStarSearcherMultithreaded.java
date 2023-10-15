package com.example.project2project2team16.searchers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AStarSearcherMultithreaded extends GreedySearcher {
    Set<ScheduleNode> createdSchedules = ConcurrentHashMap.newKeySet();
    ScheduleNode currentOptimal = null;
    Object lock = new Object();
    List<PriorityBlockingQueue<ScheduleNode>> frontiers = new ArrayList<>();
    AtomicInteger doneThreads = new AtomicInteger();
    Integer schedules = 0;

    public AStarSearcherMultithreaded(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityBlockingQueue<ScheduleNode>(100000, new ScheduleNodeAStarComparator(problem));
    }

    @Override
    public void InitialiseSearcher() {}

    protected void AddToFrontier(PriorityBlockingQueue<ScheduleNode> frontier, List<ScheduleNode> newNodes) {
        for (ScheduleNode newNode : newNodes) {
            if (createdSchedules.contains(newNode)) {
                continue;
            }

            if (!newNode.hadFixedTaskOrder) {
                if (newNode.IsEquivalent()) {
                    continue;
                }
            }

            frontier.add(newNode);
            createdSchedules.add(newNode);
        }
    }


    private ScheduleNode GetNextNode(PriorityBlockingQueue<ScheduleNode> frontier) {
        ScheduleNode node = frontier.poll();

        if (node == null) {
            return null;
        }

        if (currentOptimal != null && node.fValue >= currentOptimal.GetValue()) {
            return null;
        }

        return node;
    }

    @Override
    public ScheduleNode Search() {
        Integer threadCount = VisualisationApplication.getThreadCount();
        List<Thread> threads = new ArrayList<>();
        CountDownLatch completed = new CountDownLatch(threadCount);

        System.out.println("THREADS " + threadCount);

        for (int i = 0; i < threadCount; i++) {
            frontiers.add(new PriorityBlockingQueue(10000, new ScheduleNodeAStarComparator(problem)));
        }

        System.out.println("FRONTS " + frontiers.size());

        AddToFrontier(frontiers.get(0), Collections.singletonList(problem.GetStartNode()));

        ScheduleNode intialSearchResult = InitialSearch(threadCount);

        if (intialSearchResult != null) {
            return intialSearchResult;
        }

        DistributeFrontiers(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i;

            threads.add(
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadSearch2(threadIndex);
                }
                }));

            threads.get(i).start();
            System.out.println("RUN " + i);
        }

        for (int i = 0; i < threadCount; i++) {
            try {
                threads.get(i).join();
            }
            catch (InterruptedException e) {
                System.out.println("Thread waiting failed");
                System.out.println(e.toString());
            }
        }

//        try {
//            completed.await();
//        }
//        catch (InterruptedException e) {
//            System.out.println("Thread waiting failed");
//            System.out.println(e.toString());
//        }

        System.out.println("CHECKED  " + schedules);

        return currentOptimal;
    }

    private void ThreadSearch(Integer threadIndex, CountDownLatch completed) {
        PriorityBlockingQueue<ScheduleNode> frontier = frontiers.get(threadIndex);

        while (true) {
            ScheduleNode nextNode = GetNextNode(frontier);

            if (nextNode == null) {
                frontier.clear();
                stealWork(threadIndex);

                if (frontier.isEmpty()) {
                    System.out.println("ENDED");
                    completed.countDown();
                    return;
                }

                continue;
            }

            if (problem.IsGoal(nextNode)) {
                synchronized (lock) {
                    if (currentOptimal == null || nextNode.GetValue() < currentOptimal.GetValue()) {
                        currentOptimal = nextNode;
                    }
                }
            }
            else {
                AddToFrontier(frontier, problem.GetNeighbourStates(nextNode));
                if (nextNode.unpromisingChildren) {
                    frontier.add(nextNode);
                    nextNode.unpromisingChildren = false;
                }
            }
        }
    }

    private void ThreadSearch2(Integer threadIndex) {
        System.out.println("BEGIN " + threadIndex);

        boolean hasWork = true;
        PriorityBlockingQueue<ScheduleNode> frontier = frontiers.get(threadIndex);

        List<Integer> otherThreads = IntStream.rangeClosed(0, frontiers.size() - 1).boxed().collect(Collectors.toList());

        otherThreads.remove(threadIndex);

        Random rand = new Random();

        System.out.println("1");

        while (doneThreads.get() < frontiers.size()) {
            //System.out.println("2");
            while (!frontier.isEmpty()) {
                //System.out.println("3");
                ScheduleNode nextNode = frontier.poll();

                schedules++;

                //Sync

                if (nextNode == null) {
                    continue;
                }

                if (currentOptimal == null || nextNode.fValue < currentOptimal.GetValue()) {
                    if (problem.IsGoal(nextNode)) {
                        synchronized (lock) {
                            if (currentOptimal == null || nextNode.GetValue() < currentOptimal.GetValue()) {
                                currentOptimal = nextNode;
                            }
                        }
                    }
                    else {
                        AddToFrontier(frontier, problem.GetNeighbourStates(nextNode));
                        if (nextNode.unpromisingChildren) {
                            frontier.add(nextNode);
                            nextNode.unpromisingChildren = false;
                        }
                    }
                }
                else {
                    frontier.clear();
                }
            }

            if (hasWork) {
                doneThreads.incrementAndGet();
                hasWork = false;
            }

            System.out.println("EMPTY " + threadIndex);

            if (frontiers.size() != 1) {
                Integer victim = otherThreads.get(rand.nextInt(frontiers.size() - 1));

                System.out.println("STEAL  " + threadIndex + "  VIC " + victim);
                ScheduleNode stolen = null;

                try {
                    stolen = frontiers.get(victim).poll();
                }
                catch (IndexOutOfBoundsException e) {
                    System.out.println("ERROR");
                }


                if (stolen == null) {
                    System.out.println("STOLEN NULL");
                }
                else {
                    System.out.println("ATTEMPT " + stolen.id);
                }


                if (stolen != null && (currentOptimal == null || stolen.fValue < currentOptimal.GetValue())) {
                    frontier.add(stolen);
                    hasWork = true;
                    doneThreads.decrementAndGet();
                    System.out.println("STOLEN " + threadIndex + " " + victim);
                }
            }
        }

        System.out.println("DONE  " + threadIndex);
    }

    private void DistributeFrontiers(Integer threadCount) {
        PriorityBlockingQueue<ScheduleNode> nodes = frontiers.get(0);

        for (int i = 1; i < threadCount; i++) {
            frontiers.get(i).add(nodes.poll());
        }
    }

    public ScheduleNode InitialSearch(Integer threadCount) {
        PriorityBlockingQueue<ScheduleNode> frontier = frontiers.get(0);

        while (!frontier.isEmpty()) {
            if (frontier.size() >= threadCount) {
                return null;
            }

            ScheduleNode nextNode = GetNextNode(frontier);

            if (problem.IsGoal(nextNode)) {
                return nextNode;
            }
            else {
                AddToFrontier(frontier, problem.GetNeighbourStates(nextNode));
                if (nextNode.unpromisingChildren) {
                    frontier.add(nextNode);
                    nextNode.unpromisingChildren = false;
                }
            }
        }

        return null;
    }

    private void stealWork(Integer threadIndex) {
        //System.out.println("STEAL  " + threadIndex);

        while (true) {
            for (int i = 0; i < frontiers.size(); i++) {
                ScheduleNode node = frontiers.get(i).poll();

                if (node != null) {
                    frontiers.get(threadIndex).add(node);
                    return;
                }
            }

            if (currentOptimal != null) {
                return;
            }
        }



    }
}

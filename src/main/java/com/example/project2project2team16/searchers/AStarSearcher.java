package com.example.project2project2team16.searchers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;
import com.example.project2project2team16.utils.AppConfig;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class AStarSearcher extends GreedySearcher {
    Set<ScheduleNode> opened = ConcurrentHashMap.newKeySet();
    Set<ScheduleNode> closed = ConcurrentHashMap.newKeySet();
    ScheduleNode currentOptimal = null;
    Object lock = new Object();
    List<PriorityBlockingQueue<ScheduleNode>> frontiers = new ArrayList<>();
    AtomicInteger doneThreads = new AtomicInteger();

    public AStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityBlockingQueue<ScheduleNode>(100000, new ScheduleNodeAStarComparator(problem));
    }

    protected void AddToFrontier(PriorityBlockingQueue<ScheduleNode> frontier, List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            ScheduleNode newNode = newNodes.get(i);
            if (closed.contains(newNode) || opened.contains(newNode)) {
                continue;
            }

            frontier.add(newNode);
            opened.add(newNode);
        }
    }


    private ScheduleNode GetNextNode(PriorityBlockingQueue<ScheduleNode> frontier) {
        ScheduleNode node = frontier.poll();

        if (node == null) {
            return null;
        }

        closed.add(node);
        opened.remove(node);

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

        for (int i = 0; i < threadCount; i++) {
            frontiers.add(new PriorityBlockingQueue(10000, new ScheduleNodeAStarComparator(problem)));
        }

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
                    ThreadSearch(threadIndex);
                }
                }));

            threads.get(i).start();
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

        return currentOptimal;
    }









}

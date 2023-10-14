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

public class AStarSearcher extends GreedySearcher {
    Set<ScheduleNode> opened = ConcurrentHashMap.newKeySet();
    Set<ScheduleNode> closed = ConcurrentHashMap.newKeySet();
    ScheduleNode currentOptimal = null;
    Object lock = new Object();

    public AStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void InitialiseFrontier() {
        frontier = new PriorityBlockingQueue<ScheduleNode>(100000, new ScheduleNodeAStarComparator(problem));
    }

    @Override
    protected void AddToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            ScheduleNode newNode = newNodes.get(i);
            if (closed.contains(newNode) || opened.contains(newNode)) {
                continue;
            }

            frontier.add(newNode);
            opened.add(newNode);
        }
    }

    @Override
    protected ScheduleNode GetNextNode() {
        ScheduleNode node = ((PriorityBlockingQueue<ScheduleNode>) frontier).poll();
        closed.add(node);
        opened.remove(node);

        if (currentOptimal != null && node.GetValue() > currentOptimal.GetValue()) {
            return null;
        }

        return node;
    }

    @Override
    public ScheduleNode Search() {
        Integer threadCount = VisualisationApplication.getThreadCount();
        CountDownLatch allComplete = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadSearch(allComplete);
                }
            }).start();
        }

        try {
            allComplete.await();
        }
        catch (InterruptedException e) {
            System.out.println("Thread waiting failed");
            System.out.println(e.toString());
        }

        return currentOptimal;
    }
}

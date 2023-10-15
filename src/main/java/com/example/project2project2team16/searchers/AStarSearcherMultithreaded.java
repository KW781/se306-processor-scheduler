package com.example.project2project2team16.searchers;

import com.example.project2project2team16.VisualisationApplication;
import com.example.project2project2team16.helper.GraphVisualisationHelper;
import com.example.project2project2team16.searchers.comparators.ScheduleNodeAStarComparator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AStarSearcherMultithreaded extends GreedySearcher {
    private Set<ScheduleNode> createdSchedules = ConcurrentHashMap.newKeySet();
    private ScheduleNode currentOptimal = null;
    private Object lock = new Object();
    private List<PriorityBlockingQueue<ScheduleNode>> frontiers = new ArrayList<>();
    private AtomicInteger doneThreads = new AtomicInteger();
    private int tasksVisited = 0;
    private Object visLock = new Object();


    public AStarSearcherMultithreaded(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    protected void initialiseFrontier() {
        setFrontier(new PriorityBlockingQueue<ScheduleNode>(100000, new ScheduleNodeAStarComparator(getProblem())));
    }

    @Override
    public void initialiseSearcher() {
        ScheduleNode startNode = getProblem().getStartNode();
        SchedulingProblem.initialiseF(startNode);

        super.initialiseSearcher();

        GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
        helper.addNode(startNode, startNode.getParent());
        helper.setStartNode(startNode);
    }

    protected void AddToFrontier(PriorityBlockingQueue<ScheduleNode> frontier, List<ScheduleNode> newNodes) {
        for (ScheduleNode newNode : newNodes) {
            if (getCreatedSchedules().contains(newNode)) {
                continue;
            }

            if (!newNode.isHadFixedTaskOrder()) {
                if (newNode.isEquivalent()) {
                    continue;
                }
            }

            frontier.add(newNode);
            getCreatedSchedules().add(newNode);
        }
    }


    private ScheduleNode GetNextNode(PriorityBlockingQueue<ScheduleNode> frontier) {
        ScheduleNode node = frontier.poll();

        if (node == null || getCurrentOptimal() != null && node.getfValue() >= getCurrentOptimal().getValue()) {
            return null;
        }

        return node;
    }

    @Override
    public ScheduleNode search() {
        Integer threadCount = VisualisationApplication.getThreadCount();
        List<Thread> threads = new ArrayList<>();
//        CountDownLatch completed = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            getFrontiers().add(new PriorityBlockingQueue(10000, new ScheduleNodeAStarComparator(getProblem())));
        }

        AddToFrontier(getFrontiers().get(0), Collections.singletonList(getProblem().getStartNode()));

        ScheduleNode initialSearchResult = InitialSearch(threadCount);

        if (initialSearchResult != null) {
            return initialSearchResult;
        }

        DistributeFrontiers(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i;

            threads.add(
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadSearchPaper(threadIndex);
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


        return getCurrentOptimal();
    }

    private void threadSearch(Integer threadIndex, CountDownLatch completed) {
        PriorityBlockingQueue<ScheduleNode> frontier = getFrontiers().get(threadIndex);

        while (true) {
            ScheduleNode nextNode = GetNextNode(frontier);

            if (nextNode == null) {
                frontier.clear();
                stealWork(threadIndex);

                if (frontier.isEmpty()) {
                    completed.countDown();
                    return;
                }

                continue;
            }



            if (getProblem().isGoal(nextNode)) {
                synchronized (getLock()) {
                    if (getCurrentOptimal() == null || nextNode.getValue() < getCurrentOptimal().getValue()) {
                        setCurrentOptimal(nextNode);
                    }
                }
            }
            else {
                AddToFrontier(frontier, getProblem().getNeighbourStates(nextNode));
                if (nextNode.isUnpromisingChildren()) {
                    frontier.add(nextNode);
                    nextNode.setUnpromisingChildren(false);
                }
            }
        }
    }

    private void ThreadSearchPaper(Integer threadIndex) {
        boolean hasWork = true;
        PriorityBlockingQueue<ScheduleNode> frontier = getFrontiers().get(threadIndex);

        List<Integer> otherThreads = IntStream.rangeClosed(0, getFrontiers().size() - 1).boxed().collect(Collectors.toList());

        otherThreads.remove(threadIndex);

        Random rand = new Random();



        while (getDoneThreads().get() < getFrontiers().size()) {
            while (!frontier.isEmpty()) {
                ScheduleNode nextNode = frontier.poll();

                //Sync possible

                if (nextNode == null) {
                    continue;
                }

                if (nextNode.getVisited().size() > getTasksVisited()) {
                    synchronized (getVisLock()) {
                        if (nextNode.getVisited().size() > getTasksVisited()) {
                            nextNode.setThreadId(threadIndex);
                            GraphVisualisationHelper helper = GraphVisualisationHelper.instance();
                            helper.addNode(nextNode, nextNode.getParent());
                            helper.updateOptimalNode(nextNode);
                            setTasksVisited(nextNode.getVisited().size());
                        }
                    }
                }

                if (getCurrentOptimal() == null || nextNode.getfValue() < getCurrentOptimal().getValue()) {
                    if (getProblem().isGoal(nextNode)) {
                        synchronized (getLock()) {
                            if (getCurrentOptimal() == null || nextNode.getValue() < getCurrentOptimal().getValue()) {
                                setCurrentOptimal(nextNode);
                            }
                        }
                    }
                    else {
                        AddToFrontier(frontier, getProblem().getNeighbourStates(nextNode));
                        if (nextNode.isUnpromisingChildren()) {
                            frontier.add(nextNode);
                            nextNode.setUnpromisingChildren(false);
                        }
                    }
                }
                else {
                    frontier.clear();
                }
            }

            if (hasWork) {
                getDoneThreads().incrementAndGet();
                hasWork = false;
            }

            if (getFrontiers().size() != 1) {
                Integer victim = otherThreads.get(rand.nextInt(getFrontiers().size() - 1));

                ScheduleNode stolen = getFrontiers().get(victim).poll();

                if (stolen != null && (getCurrentOptimal() == null || stolen.getfValue() < getCurrentOptimal().getValue())) {
                    frontier.add(stolen);
                    hasWork = true;
                    getDoneThreads().decrementAndGet();
                }
            }
        }
    }

    private void DistributeFrontiers(Integer threadCount) {
        PriorityBlockingQueue<ScheduleNode> nodes = getFrontiers().get(0);

        for (int i = 1; i < threadCount; i++) {
            getFrontiers().get(i).add(nodes.poll());
        }
    }

    public ScheduleNode InitialSearch(Integer threadCount) {
        PriorityBlockingQueue<ScheduleNode> frontier = getFrontiers().get(0);

        while (!frontier.isEmpty()) {
            if (frontier.size() >= threadCount) {
                return null;
            }

            ScheduleNode nextNode = GetNextNode(frontier);

            if (getProblem().isGoal(nextNode)) {
                return nextNode;
            }
            else {
                AddToFrontier(frontier, getProblem().getNeighbourStates(nextNode));
                if (nextNode.isUnpromisingChildren()) {
                    frontier.add(nextNode);
                    nextNode.setUnpromisingChildren(false);
                }
            }
        }

        return null;
    }

    private void stealWork(Integer threadIndex) {

        while (true) {
            for (int i = 0; i < getFrontiers().size(); i++) {
                ScheduleNode node = getFrontiers().get(i).poll();

                if (node != null) {
                    getFrontiers().get(threadIndex).add(node);
                    return;
                }
            }

            if (getCurrentOptimal() != null) {
                return;
            }
        }

    }

    public Set<ScheduleNode> getCreatedSchedules() {
        return createdSchedules;
    }

    public void setCreatedSchedules(Set<ScheduleNode> createdSchedules) {
        this.createdSchedules = createdSchedules;
    }

    public ScheduleNode getCurrentOptimal() {
        return currentOptimal;
    }

    public void setCurrentOptimal(ScheduleNode currentOptimal) {
        this.currentOptimal = currentOptimal;
    }

    public Object getLock() {
        return lock;
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }

    public List<PriorityBlockingQueue<ScheduleNode>> getFrontiers() {
        return frontiers;
    }

    public void setFrontiers(List<PriorityBlockingQueue<ScheduleNode>> frontiers) {
        this.frontiers = frontiers;
    }

    public AtomicInteger getDoneThreads() {
        return doneThreads;
    }

    public void setDoneThreads(AtomicInteger doneThreads) {
        this.doneThreads = doneThreads;
    }

    public int getTasksVisited() {
        return tasksVisited;
    }

    public void setTasksVisited(int tasksVisited) {
        this.tasksVisited = tasksVisited;
    }

    public Object getVisLock() {
        return visLock;
    }

    public void setVisLock(Object visLock) {
        this.visLock = visLock;
    }
}

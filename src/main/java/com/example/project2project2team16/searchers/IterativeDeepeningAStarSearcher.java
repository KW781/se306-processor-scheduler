package com.example.project2project2team16.searchers;

import java.util.Collections;
import java.util.List;

public class IterativeDeepeningAStarSearcher extends AStarSearcher {
    private Integer evalLimit = 0;
    private Integer nextEvalLimit = 0;

    public IterativeDeepeningAStarSearcher(SchedulingProblem problem) {
        super(problem);
    }

    @Override
    public void initialiseSearcher() {
        super.initialiseSearcher();
        setNextEvalLimit(getProblem().getStartNode().getfValue());
        setEvalLimit(getProblem().getStartNode().getfValue());
    }

    @Override
    protected void addToFrontier(List<ScheduleNode> newNodes) {
        for (int i = newNodes.size() - 1; i >= 0; i--) {
            ScheduleNode newNode = newNodes.get(i);

            if (newNode.getfValue() <= getEvalLimit()) {
                pruneOrAdd(newNode);
            }
            else {
                setNextEvalLimit(Math.min(getNextEvalLimit(), newNode.getfValue()));
            }
        }
    }

    @Override
    public ScheduleNode search() {
        ScheduleNode result = null;

        while (result == null) {
            result = super.search();
            getCreatedSchedules().clear();
            setEvalLimit(getNextEvalLimit());
            setNextEvalLimit(Integer.MAX_VALUE);
            ScheduleNode startNode = getProblem().getStartNode();
            SchedulingProblem.initialiseF(startNode);
            addToFrontier(Collections.singletonList(startNode));
            setSchedulesAdded(0);
            setDups(0);
            setSchedulesExplored(0);
        }

        return result;
    }

    public Integer getEvalLimit() {
        return evalLimit;
    }

    public void setEvalLimit(Integer evalLimit) {
        this.evalLimit = evalLimit;
    }

    public Integer getNextEvalLimit() {
        return nextEvalLimit;
    }

    public void setNextEvalLimit(Integer nextEvalLimit) {
        this.nextEvalLimit = nextEvalLimit;
    }
}

package myparty.agentgg.searchalgorithms;

import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.DiscreteValue;
import geniusweb.issuevalue.NumberValue;
import geniusweb.issuevalue.Value;

import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

public class SearchSpaceBid implements LocalSearchSpace<Bid> {
    public static final int DEFAULT_VALUE = 1;
    private final List<Bid> bids;
    private final int k;

    public SearchSpaceBid(List<Bid> bids, int numberClosestNeighbors) {
        this.bids = bids;
        this.k = numberClosestNeighbors;
    }

    @Override
    public Set<Bid> getNeighbors(Bid bid) {
        return getNearestNeighborsOfPoint(bids, bid, k);
    }

    private static Set<Bid> getNearestNeighborsOfPoint(List<Bid> neighbors, Bid bid, int numberOfNeighbors) {
        return neighbors.stream()
                .filter(neighbor -> !bid.equals(neighbor))
                .sorted(comparing(neighbor -> calcDistanceBetweenBids(bid, neighbor)))
                .limit(numberOfNeighbors)
                .collect(toSet());
    }

    private static double calcDistanceBetweenBids(Bid bid1, Bid bid2) {
        return bid1.getIssues().stream()
                .mapToDouble(issue -> calcDistanceBetweenValues(bid1.getValue(issue), bid2.getValue(issue)))
                .sum();
    }

    private static double calcDistanceBetweenValues(Value value1, Value value2) {
        if (value1 instanceof NumberValue && value2 instanceof NumberValue)
            return Math.abs(((NumberValue) value1).getValue().doubleValue() - ((NumberValue) value2).getValue().doubleValue());
        else if (value1 instanceof DiscreteValue && value2 instanceof DiscreteValue)
            return ((DiscreteValue) value1).getValue().equals(((DiscreteValue) value2).getValue()) ? 0 : 1;
        return DEFAULT_VALUE;
    }

    @Override
    public Bid createRandomState() {
        return null;
    }
}

package myparty.agentgg.searchalgorithms;

import geniusweb.bidspace.AllBidsList;
import geniusweb.issuevalue.Bid;
import geniusweb.issuevalue.DiscreteValue;
import geniusweb.issuevalue.Value;
import myparty.agentgg.ImpMap;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class SearchSpaceBid {
    public static final int DEFAULT_VALUE = 1;
    private final List<Bid> bids;
    private final int k;

    public SearchSpaceBid(List<Bid> bids, int numberClosestNeighbors) {
        this.bids = bids;
        this.k = numberClosestNeighbors;
    }

    public Set<Bid> getNeighbors(Bid bid) {
        return new HashSet(getNearestNeighborsOfPoint(bids, bid, k));
    }

    public static List<Bid> getNearestNeighborsOfPoint(List<Bid> neighbors, Bid bid, int numberOfNeighbors) {
        return neighbors.stream()
                .filter(neighbor -> !bid.equals(neighbor))
                .sorted(comparing(neighbor -> calcDistanceBetweenBids(bid, neighbor)))
                .limit(numberOfNeighbors)
                .collect(Collectors.toList());
    }

    private static double calcDistanceBetweenBids(Bid bid1, Bid bid2) {
        return bid1.getIssues().stream()
                .mapToDouble(issue -> calcDistanceBetweenValues(bid1.getValue(issue), bid2.getValue(issue)))
                .sum();
    }

    private static double calcDistanceBetweenValues(Value value1, Value value2) {
        if (isNumber(value1) && isNumber(value2))
            return Math.abs(getValue(value1) - getValue(value2));
        else if (value1 instanceof DiscreteValue && value2 instanceof DiscreteValue)
            return ((DiscreteValue) value1).getValue().equals(((DiscreteValue) value2).getValue()) ? 0 : 1;
        return DEFAULT_VALUE;
    }

    private static double getValue(Value value) {
        return Double.parseDouble(((DiscreteValue) value).getValue());
    }

    private static boolean isNumber(Value value) {
        try {
            Double.valueOf(((DiscreteValue) value).getValue());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Bid createRandomState(ImpMap impMap, AllBidsList allBidsList, double lowerThreshold) {
        while (true) {
            Bid bid = generateRandomBid(allBidsList);
            if (impMap.getImportance(bid) >= lowerThreshold) {
                return bid;
            }
        }
    }

    public Bid generateRandomBid(AllBidsList allbids) {
        return allbids.get(new Random().nextInt(allbids.size().intValue()));
    }
}

package myparty.agentgg.searchalgorithms;

import geniusweb.issuevalue.Bid;
import myparty.agentgg.SimpleLinearOrdering;

public class FitnessBid implements FitnessAnalyzer<Bid> {


    private final SimpleLinearOrdering simpleLinearOrdering;

    public FitnessBid(SimpleLinearOrdering simpleLinearOrdering) {
        this.simpleLinearOrdering = simpleLinearOrdering;
    }

    @Override
    public double calcFitness(Bid bid) {
        return simpleLinearOrdering.getUtility(bid).doubleValue();
    }
}

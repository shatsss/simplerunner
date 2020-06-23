package myparty.agentgg.searchalgorithms;

import geniusweb.bidspace.AllBidsList;
import geniusweb.issuevalue.Bid;
import myparty.agentgg.ImpMap;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static java.lang.Math.exp;
import static java.lang.Math.random;
import static myparty.agentgg.searchalgorithms.SearchAlgorithm.chooseRandom;

public class SimulatedAnnealing {
    private final int maxNumIterations;
    private final double initialTemperature;
    private final double temperatureChange;
    private SearchAlgorithm.FitnessAnalyzedState<Bid> bestState;
    private SearchAlgorithm.FitnessAnalyzedState<Bid> currentState;
    private double temperature;
    private Random rand;

    public SimulatedAnnealing(int maxNumIterations, double initialTemperature, double temperatureChange, long seed) {
        validateInput(maxNumIterations, initialTemperature, temperatureChange);
        this.maxNumIterations = maxNumIterations;
        this.initialTemperature = initialTemperature;
        this.temperatureChange = temperatureChange;
        this.rand = new Random(seed);
    }

    private void validateInput(int maxNumIterations, double initialTemperature, double temperatureChange) {
        validate(maxNumIterations > 0, "Max num of iterations must be positive!");
        validate(initialTemperature >= 0, "Initial temperature must be positive!");
        validate(temperatureChange >= 0 && temperatureChange <= 1, "Temperature change must be between 0 and 1!");
    }

    public Bid search(FitnessBid fitnessFunction, SearchSpaceBid localSearchSpace, ImpMap impMap, AllBidsList allBidsList, double lowerThreshold) {
        Bid initialState = localSearchSpace.createRandomState(impMap, allBidsList, lowerThreshold);
        bestState = new SearchAlgorithm.FitnessAnalyzedState<>(initialState, fitnessFunction.calcFitness(initialState));
        currentState = bestState;
        temperature = initialTemperature;
        for (int currentIteration = 0; currentIteration < maxNumIterations; currentIteration++) {
            Set<Bid> neighbors = localSearchSpace.getNeighbors(currentState.getState());
            Optional<SearchAlgorithm.FitnessAnalyzedState<Bid>> neighbor = getRandomNeighbor(neighbors, fitnessFunction, this.rand.nextInt());
            if (!neighbor.isPresent()) break;
            updateStates(neighbor.get());
            updateTemperature();
        }
        return bestState.getState();
    }

    private void updateStates(SearchAlgorithm.FitnessAnalyzedState<Bid> neighbor) {
        if (neighbor.getFitness() >= currentState.getFitness()) {
            currentState = neighbor;
            if (neighbor.getFitness() >= bestState.getFitness()) bestState = currentState;
        } else if (exp((neighbor.getFitness() - currentState.getFitness()) / temperature) > random())
            currentState = neighbor;
    }

    private void updateTemperature() {
        temperature = temperature * temperatureChange;
    }

    public static void validate(boolean validCondition, String errorMessage) {
        if (!validCondition) {
            throw new RuntimeException(errorMessage);
        }
    }

    private static Optional<SearchAlgorithm.FitnessAnalyzedState<Bid>> getRandomNeighbor(Set<Bid> neighbors, FitnessAnalyzer<Bid> fitnessAnalyzer, long seed) {
        if (neighbors.size() == 0) return Optional.empty();
        Bid neighbor = (Bid) chooseRandom(new ArrayList(neighbors), seed);
        return Optional.of(new SearchAlgorithm.FitnessAnalyzedState<>(neighbor, fitnessAnalyzer.calcFitness(neighbor)));
    }

}

package myparty.agentgg.searchalgorithms;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static java.lang.Math.exp;
import static java.lang.Math.random;

public class SimulatedAnnealing<S> implements SearchAlgorithm<S> {
    private final int maxNumIterations;
    private final double initialTemperature;
    private final double temperatureChange;
    private FitnessAnalyzedState<S> bestState;
    private FitnessAnalyzedState<S> currentState;
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

    @Override
    public S search(FitnessAnalyzer<S> fitnessFunction, LocalSearchSpace<S> localSearchSpace, S initialState) {
        bestState = new FitnessAnalyzedState<>(initialState, fitnessFunction.calcFitness(initialState));
        currentState = bestState;
        temperature = initialTemperature;
        for (int currentIteration = 0; currentIteration < maxNumIterations; currentIteration++) {
            Set<S> neighbors = localSearchSpace.getNeighbors(currentState.getState());
            Optional<FitnessAnalyzedState<S>> neighbor = getRandomNeighbor(neighbors, fitnessFunction, this.rand.nextInt());
            if (!neighbor.isPresent()) break;
            updateStates(neighbor.get());
            updateTemperature();
        }
        return bestState.getState();
    }

    private void updateStates(FitnessAnalyzedState<S> neighbor) {
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
}

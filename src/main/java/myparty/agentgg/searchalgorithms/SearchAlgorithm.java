package myparty.agentgg.searchalgorithms;



import javafx.util.Pair;

import java.util.*;


public interface SearchAlgorithm<S> {
    S search(FitnessAnalyzer<S> fitnessAnalyzer, LocalSearchSpace<S> localSearchSpace, S initialState);

    default S search(FitnessAnalyzer<S> fitnessAnalyzer, LocalSearchSpace<S> localSearchSpace) {
        return search(fitnessAnalyzer, localSearchSpace, localSearchSpace.createRandomState());
    }

    default FitnessAnalyzedState<S> createInitialState(FitnessAnalyzer<S> fitnessAnalyzer, S initialState) {
        double initialStateScore = fitnessAnalyzer.calcFitness(initialState);
        return new FitnessAnalyzedState<>(initialState, initialStateScore);
    }


    public static <R> R chooseRandom(List<R> list, long seed) {
        return list.get(new Random(seed * 10000).nextInt(list.size()));
    }
    default Optional<FitnessAnalyzedState<S>> getRandomNeighbor(Set<S> neighbors, FitnessAnalyzer<S> fitnessAnalyzer, long seed) {
        if (neighbors.size() == 0) return Optional.empty();
        S neighbor = chooseRandom(new ArrayList<>(neighbors), seed);
        return Optional.of(new FitnessAnalyzedState<>(neighbor, fitnessAnalyzer.calcFitness(neighbor)));
    }

    class FitnessAnalyzedState<S> extends Pair<S, Double> {
        public FitnessAnalyzedState(S key, Double value) {
            super(key, value);
        }

        public S getState() {
            return getKey();
        }

        public double getFitness() {
            return getValue();
        }
    }

}
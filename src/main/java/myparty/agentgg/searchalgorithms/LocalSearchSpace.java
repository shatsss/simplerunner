package myparty.agentgg.searchalgorithms;

import java.util.Set;

public interface LocalSearchSpace<S> {
    Set<S> getNeighbors(S state);

    S createRandomState();
}
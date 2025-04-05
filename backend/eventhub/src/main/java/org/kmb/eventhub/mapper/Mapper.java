package org.kmb.eventhub.mapper;

public interface Mapper<S,T> {
    T map(S source);
}

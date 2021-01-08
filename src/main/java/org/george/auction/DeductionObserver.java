package org.george.auction;

public interface DeductionObserver {

    boolean deductionNotify(Integer playerId, Integer num);
}

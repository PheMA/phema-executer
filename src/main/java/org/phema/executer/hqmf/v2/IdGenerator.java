package org.phema.executer.hqmf.v2;

/**
 * Created by Luke Rasmussen on 8/21/17.
 */
public class IdGenerator {
    private int currentId = 0;

    public IdGenerator() {
        currentId = 0;
    }

    public int nextId() {
        currentId += 1;
        return currentId;
    }
}

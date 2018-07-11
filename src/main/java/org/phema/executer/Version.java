package org.phema.executer;

/**
 * Created by Luke Rasmussen on 7/11/18.
 */
public class Version {
    public void Version() {}
    public String toString() {
        return getClass().getPackage().getImplementationVersion();
    }
}


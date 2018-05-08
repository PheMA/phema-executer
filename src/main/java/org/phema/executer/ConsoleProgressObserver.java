package org.phema.executer;

import org.phema.executer.translator.HqmfToI2b2;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Luke Rasmussen on 4/27/18.
 */
public class ConsoleProgressObserver implements Observer {
    @Override
    public void update(Observable observable, Object arg) {
        System.out.println((String)arg);
    }
}

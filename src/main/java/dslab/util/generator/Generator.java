package dslab.util.generator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Generator {

    private final static AtomicInteger counter = new AtomicInteger();

    public static long getID() {
        return counter.getAndIncrement();
    }

}

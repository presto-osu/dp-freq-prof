/*
 * Timer.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto;

public class Timer {
    private long start;

    Timer() {
        start = System.currentTimeMillis();
    }

    public void reset() {
        start = System.currentTimeMillis();
    }

    public long duration() {
        return System.currentTimeMillis() - start;
    }
}

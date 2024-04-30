package com.googlecode.crowdin.maven.dao;

import org.apache.maven.plugin.logging.Log;

import java.util.logging.Logger;

public class GazelleLogger extends Logger {

    private final Log log;

    public GazelleLogger(Log log) {
        super(null, null);
        this.log = log;
    }

    @Override
    public void fine(String message) {
        log.debug(message);
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warning(String message) {
        log.warn(message);
    }

    @Override
    public void severe(String message) {
        log.error(message);
    }


}

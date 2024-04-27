package com.googlecode.crowdin.maven.dao;

public class CrowdinDAOException extends RuntimeException {

        public CrowdinDAOException(String message) {
            super(message);
        }

        public CrowdinDAOException(String message, Throwable cause) {
            super(message, cause);
        }
}

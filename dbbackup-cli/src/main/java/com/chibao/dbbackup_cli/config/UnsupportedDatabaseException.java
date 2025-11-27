package com.chibao.dbbackup_cli.config;

class UnsupportedDatabaseException extends RuntimeException {
    public UnsupportedDatabaseException(String message) {
        super(message);
    }
}
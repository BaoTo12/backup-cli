package com.chibao.dbbackup_cli.domain.exception;

public class RestoreFailedException extends RuntimeException {
    public RestoreFailedException(String message) {
        super(message);
    }
}

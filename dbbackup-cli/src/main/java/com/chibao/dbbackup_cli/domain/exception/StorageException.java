package com.chibao.dbbackup_cli.domain.exception;

public class StorageException extends RuntimeException {
    public StorageException() {
        super();
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

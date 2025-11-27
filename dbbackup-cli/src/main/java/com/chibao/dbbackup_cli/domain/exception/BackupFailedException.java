package com.chibao.dbbackup_cli.domain.exception;

public class BackupFailedException extends RuntimeException {
    public BackupFailedException() {
        super();
    }

    public BackupFailedException(String message) {
        super(message);
    }

    public BackupFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

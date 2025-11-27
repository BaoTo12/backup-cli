package com.chibao.dbbackup_cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DbBackupCliApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbBackupCliApplication.class, args);
	}

}

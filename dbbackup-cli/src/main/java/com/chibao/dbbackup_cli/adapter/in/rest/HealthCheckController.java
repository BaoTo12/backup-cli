package com.chibao.dbbackup_cli.adapter.in.rest;

import com.chibao.dbbackup_cli.adapter.in.rest.dto.HealthResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check Controller
 * Additional endpoints for monitoring
 */
@RestController
@RequestMapping("/api/v1/health")
@Slf4j
class HealthCheckController {

    @GetMapping
    public ResponseEntity<HealthResponseDto> health() {
        return ResponseEntity.ok(
                HealthResponseDto.builder()
                        .status("UP")
                        .timestamp(java.time.Instant.now().toString())
                        .version("1.0.0")
                        .build()
        );
    }
}

package com.chibao.dbbackup_cli.config;

import com.chibao.dbbackup_cli.domain.port.out.StoragePort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Storage Factory
 *
 * Automatically discovers all StoragePort implementations
 * and provides factory method to get adapter by provider type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class StorageFactory {

    // Spring automatically injects ALL beans implementing StoragePort
    private final List<StoragePort> storageAdapters;

    private Map<String, StoragePort> adapterMap;

    @PostConstruct
    public void init() {
        adapterMap = storageAdapters.stream()
                .collect(Collectors.toMap(
                        StoragePort::getProviderType,
                        Function.identity()
                ));

        log.info("Registered storage adapters: {}", adapterMap.keySet());
    }

    public StoragePort getAdapter(String providerType) {
        StoragePort adapter = adapterMap.get(providerType.toLowerCase());

        if (adapter == null) {
            String supportedTypes = String.join(", ", adapterMap.keySet());
            throw new IllegalArgumentException(
                    String.format(
                            "Storage provider '%s' is not supported. Supported providers: %s",
                            providerType,
                            supportedTypes
                    )
            );
        }

        return adapter;
    }

    public List<String> getSupportedProviders() {
        return List.copyOf(adapterMap.keySet());
    }
}
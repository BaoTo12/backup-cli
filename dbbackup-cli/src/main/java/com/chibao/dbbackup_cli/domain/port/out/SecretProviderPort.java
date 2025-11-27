package com.chibao.dbbackup_cli.domain.port.out;

/**
 * OUTBOUND PORT: Secret Provider Port
 * Core cần retrieve credentials securely
 */
public interface SecretProviderPort {

    /**
     * Get secret value
     * @param key secret key/path
     * @return secret value
     */
    String getSecret(String key);

    /**
     * Get secret with default value
     * @param key secret key
     * @param defaultValue default if not found
     * @return secret value or default
     */
    String getSecret(String key, String defaultValue);

    /**
     * Check if provider supports this key
     * @param key secret key
     * @return true if supported
     */
    boolean supports(String key);

    /**
     * Get provider type
     * @return provider type (vault, env, kms)
     */
    String getProviderType();
}

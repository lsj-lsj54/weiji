package com.weiji.framework.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheMessage {

    private String instanceId;
    private String cacheName;
    private String key;
    /** When true, {@link #key} is a prefix; other instances evict matching L1 keys. */
    private boolean prefix;
}

package com.weiji.framework.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiji.config.CacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationListener implements MessageListener {

    private final CacheProperties properties;
    private final MultiLevelCache multiLevelCache;
    private final ObjectMapper objectMapper = MultiLevelCache.cacheObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            CacheMessage msg = objectMapper.readValue(message.getBody(), CacheMessage.class);
            if (msg == null || StringUtils.isBlank(msg.getCacheName())) {
                return;
            }
            if (StringUtils.equals(properties.getInstanceId(), msg.getInstanceId())) {
                return;
            }
            if (msg.isPrefix()) {
                multiLevelCache.invalidateL1Prefix(msg.getCacheName(), StringUtils.defaultString(msg.getKey()));
                return;
            }
            if (StringUtils.isBlank(msg.getKey())) {
                multiLevelCache.invalidateL1(msg.getCacheName());
                return;
            }
            multiLevelCache.invalidateL1(msg.getCacheName(), msg.getKey());
        } catch (Exception ex) {
            log.warn("ignore cache invalidate message", ex);
        }
    }
}

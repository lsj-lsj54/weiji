package com.weiji.common.result;

import com.weiji.common.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void okWithoutData() {
        Result<Void> result = Result.ok();
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertNull(result.getData());
    }
}

package com.weiji.modules.knowledge.service.review;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EbbinghausReviewScheduler {

    private static final List<Integer> INTERVALS = List.of(1, 2, 4, 7, 15);

    public int nextInterval(int current, boolean remembered) {
        if (!remembered) {
            return 1;
        }
        for (int i = 0; i < INTERVALS.size(); i++) {
            if (current < INTERVALS.get(i)) {
                return INTERVALS.get(i);
            }
            if (current == INTERVALS.get(i) && i < INTERVALS.size() - 1) {
                return INTERVALS.get(i + 1);
            }
        }
        return 15;
    }
}

package org.example.backend.common;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TrendPoint {
    private LocalDateTime time;
    private BigDecimal value;

    public static TrendPoint of(LocalDateTime time, BigDecimal value) {
        TrendPoint p = new TrendPoint();
        p.time = time;
        p.value = value;
        return p;
    }
}

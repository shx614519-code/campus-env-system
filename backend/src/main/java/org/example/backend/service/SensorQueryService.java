package org.example.backend.service;

import org.example.backend.common.PageResult;
import org.example.backend.common.TrendPoint;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorQueryService {
    Object latest(String type, String devid);

    PageResult<?> list(String type, String devid, long page, long size);

    List<TrendPoint> trend(String type, String devid, String metric, LocalDateTime start, LocalDateTime end);
}

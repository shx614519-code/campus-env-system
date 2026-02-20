package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.common.PageResult;
import org.example.backend.entity.AirSensorData;
import org.example.backend.entity.SoilSensorData;
import org.example.backend.entity.WaterSensorData;
import org.example.backend.mapper.AirSensorDataMapper;
import org.example.backend.mapper.SoilSensorDataMapper;
import org.example.backend.mapper.WaterSensorDataMapper;
import org.example.backend.service.SensorQueryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SensorQueryServiceImpl implements SensorQueryService {

    private final AirSensorDataMapper airMapper;
    private final SoilSensorDataMapper soilMapper;
    private final WaterSensorDataMapper waterMapper;

    public SensorQueryServiceImpl(AirSensorDataMapper airMapper,
                                  SoilSensorDataMapper soilMapper,
                                  WaterSensorDataMapper waterMapper) {
        this.airMapper = airMapper;
        this.soilMapper = soilMapper;
        this.waterMapper = waterMapper;
    }

    @Override
    public Object latest(String type, String devid) {
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("type 不能为空（air/soil/water）");
        }
        type = type.trim().toLowerCase();

        switch (type) {
            case "air" -> {
                QueryWrapper<AirSensorData> qw = new QueryWrapper<>();

                if (StringUtils.hasText(devid)) {
                    qw.eq("devid", devid.trim());
                }

                qw.isNotNull("windDir")
                        .isNotNull("windSpeed")
                        .isNotNull("airTemp")
                        .isNotNull("airHumi")
                        .isNotNull("airPres")
                        .isNotNull("airLigh")
                        .isNotNull("airPM25")
                        .isNotNull("airPM10");

                qw.orderByDesc("created_at").last("LIMIT 1");

                return airMapper.selectOne(qw);
            }
            case "soil" -> {
                QueryWrapper<SoilSensorData> qw = new QueryWrapper<>();

                if (StringUtils.hasText(devid)) {
                    qw.eq("devid", devid.trim());
                }

                qw
                        .isNotNull("nitrogen")
                        .isNotNull("phosphorus")
                        .isNotNull("potassium")
                        .isNotNull("electrical")
                        .isNotNull("ph")
                        .isNotNull("humidity")
                        .isNotNull("temperature")
                        .isNotNull("battery_level")
                        .isNotNull("longitude")
                        .isNotNull("latitude")
                        .isNotNull("collection_id")
                        .isNotNull("created_at");

                qw.orderByDesc("created_at");

                return soilMapper.selectOne(qw);
            }
            case "water" -> {
                QueryWrapper<WaterSensorData> qw = new QueryWrapper<>();

                if (StringUtils.hasText(devid)) {
                    qw.eq("devid", devid.trim());
                }

                qw
                        .isNotNull("water_turb")
                        .isNotNull("water_oxysat")
                        .isNotNull("water_oxycon")
                        .isNotNull("water_temp")
                        .isNotNull("water_ph")
                        .isNotNull("water_ec")
                        .isNotNull("water_sal")
                        .isNotNull("water_tds")
                        .isNotNull("battery_level")
                        .isNotNull("longitude")
                        .isNotNull("latitude")
                        .isNotNull("collection_id")
                        .isNotNull("created_at");

                qw.orderByDesc("created_at");

                return waterMapper.selectOne(qw);
            }
            default -> throw new IllegalArgumentException("type 只能是 air / soil / water");
        }
    }

    @Override
    public PageResult<?> list(String type, String devid, long page, long size) {
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("type 不能为空（air/soil/water）");
        }
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 200) size = 200;


        type = type.trim().toLowerCase();

        switch (type) {
            case "air" -> {
                QueryWrapper<AirSensorData> qw = new QueryWrapper<>();
                if (StringUtils.hasText(devid)) qw.eq("devid", devid.trim());
                qw.orderByDesc("created_at");
                Page<AirSensorData> p = airMapper.selectPage(new Page<>(page, size), qw);
                return PageResult.of(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
            }
            case "soil" -> {
                QueryWrapper<SoilSensorData> qw = new QueryWrapper<>();
                if (StringUtils.hasText(devid)) qw.eq("devid", devid.trim());
                qw.orderByDesc("created_at");
                Page<SoilSensorData> p = soilMapper.selectPage(new Page<>(page, size), qw);
                return PageResult.of(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
            }
            case "water" -> {
                QueryWrapper<WaterSensorData> qw = new QueryWrapper<>();
                if (StringUtils.hasText(devid)) qw.eq("devid", devid.trim());
                qw.orderByDesc("created_at");
                Page<WaterSensorData> p = waterMapper.selectPage(new Page<>(page, size), qw);
                return PageResult.of(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
            }
            default -> throw new IllegalArgumentException("type 只能是 air / soil / water");
        }
    }

    @Override
    public java.util.List<org.example.backend.common.TrendPoint> trend(
            String type, String devid, String metric,
            java.time.LocalDateTime start, java.time.LocalDateTime end) {

        if (!org.springframework.util.StringUtils.hasText(type)) {
            throw new IllegalArgumentException("type 不能为空（air/soil/water）");
        }
        if (!org.springframework.util.StringUtils.hasText(metric)) {
            throw new IllegalArgumentException("metric 不能为空（例如 airTemp/humidity/waterPh）");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("start 和 end 不能为空");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end 必须 >= start");
        }

        type = type.trim().toLowerCase();
        final String metricKey = metric.trim();

        java.util.Map<String, String> airMetrics = java.util.Map.ofEntries(
                java.util.Map.entry("airTemp", "airTemp"),
                java.util.Map.entry("airHumi", "airHumi"),
                java.util.Map.entry("airPres", "airPres"),
                java.util.Map.entry("airPM25", "airPM25"),
                java.util.Map.entry("airPM10", "airPM10"),
                java.util.Map.entry("windSpeed", "windSpeed"),
                java.util.Map.entry("windDir", "windDir")
        );

        java.util.Map<String, String> soilMetrics = java.util.Map.ofEntries(
                java.util.Map.entry("temperature", "temperature"),
                java.util.Map.entry("humidity", "humidity"),
                java.util.Map.entry("ph", "ph"),
                java.util.Map.entry("electrical", "electrical"),
                java.util.Map.entry("nitrogen", "nitrogen"),
                java.util.Map.entry("phosphorus", "phosphorus"),
                java.util.Map.entry("potassium", "potassium")
        );

        java.util.Map<String, String> waterMetrics = java.util.Map.ofEntries(
                java.util.Map.entry("waterTemp", "waterTemp"),
                java.util.Map.entry("waterPh", "waterPh"),
                java.util.Map.entry("waterEc", "waterEc"),
                java.util.Map.entry("waterTds", "waterTds"),
                java.util.Map.entry("waterSal", "waterSal"),
                java.util.Map.entry("waterTurb", "waterTurb"),
                java.util.Map.entry("waterOxycon", "waterOxycon"),
                java.util.Map.entry("waterOxysat", "waterOxysat")
        );

        final String column;
        switch (type) {
            case "air" -> column = airMetrics.get(metricKey);
            case "soil" -> column = soilMetrics.get(metricKey);
            case "water" -> column = waterMetrics.get(metricKey);
            default -> throw new IllegalArgumentException("type 只能是 air / soil / water");
        }

        if (column == null) {
            throw new IllegalArgumentException("不支持的 metric：" + metricKey);
        }

        switch (type) {
            case "air" -> {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.example.backend.entity.AirSensorData> qw =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                qw.orderByAsc("created_at");
                if (org.springframework.util.StringUtils.hasText(devid)) qw.eq("devid", devid.trim());
                qw.between("created_at", start, end);
                qw.orderByAsc("created_at");
                java.util.List<org.example.backend.entity.AirSensorData> list = airMapper.selectList(qw);
                return list.stream()
                        .map(x -> org.example.backend.common.TrendPoint.of(
                                x.getCreatedAt(), getBigDecimalField(x, metricKey)))
                        .toList();
            }
            case "soil" -> {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.example.backend.entity.SoilSensorData> qw =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                qw.orderByAsc("created_at");
                if (org.springframework.util.StringUtils.hasText(devid)) qw.eq("devid", devid.trim());
                qw.between("created_at", start, end);
                qw.orderByAsc("created_at");
                java.util.List<org.example.backend.entity.SoilSensorData> list = soilMapper.selectList(qw);
                return list.stream()
                        .map(x -> org.example.backend.common.TrendPoint.of(
                                x.getCreatedAt(), getBigDecimalField(x, metricKey)))
                        .toList();
            }
            case "water" -> {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.example.backend.entity.WaterSensorData> qw =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                qw.orderByAsc("created_at");
                if (org.springframework.util.StringUtils.hasText(devid)) qw.eq("devid", devid.trim());
                qw.between("created_at", start, end);
                qw.orderByAsc("created_at");
                java.util.List<org.example.backend.entity.WaterSensorData> list = waterMapper.selectList(qw);
                return list.stream()
                        .map(x -> org.example.backend.common.TrendPoint.of(
                                x.getCreatedAt(), getBigDecimalField(x, metricKey)))
                        .toList();
            }
            default -> throw new IllegalArgumentException("type 只能是 air / soil / water");
        }
    }


    private java.math.BigDecimal getBigDecimalField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object v = f.get(obj);
            if (v == null) return null;
            if (v instanceof java.math.BigDecimal bd) return bd;
            if (v instanceof Number n) return new java.math.BigDecimal(n.toString());
            return null;
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

}

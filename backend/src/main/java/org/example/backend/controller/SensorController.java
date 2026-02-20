package org.example.backend.controller;

import org.example.backend.common.PageResult;
import org.example.backend.common.R;
import org.example.backend.service.SensorQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class SensorController {

    private final SensorQueryService sensorQueryService;

    public SensorController(SensorQueryService sensorQueryService) {
        this.sensorQueryService = sensorQueryService;
    }

    @GetMapping("/api/sensor/latest")
    public R<?> latest(@RequestParam String type,
                       @RequestParam(required = false) String devid) {
        Object data = sensorQueryService.latest(type, devid);
        return R.ok(data);
    }

    @GetMapping("/api/sensor/list")
    public R<PageResult<?>> list(@RequestParam String type,
                                 @RequestParam(required = false) String devid,
                                 @RequestParam(defaultValue = "1") long page,
                                 @RequestParam(defaultValue = "20") long size) {
        PageResult<?> data = sensorQueryService.list(type, devid, page, size);
        return R.ok(data);
    }

    @GetMapping("/api/sensor/trend")
    public R<?> trend(@RequestParam String type,
                      @RequestParam String metric,
                      @RequestParam String start,
                      @RequestParam String end,
                      @RequestParam(required = false) String devid) {

        LocalDateTime s = LocalDateTime.parse(start.replace(" ", "T"));
        LocalDateTime e = LocalDateTime.parse(end.replace(" ", "T"));

        return R.ok(sensorQueryService.trend(type, devid, metric, s, e));
    }


}

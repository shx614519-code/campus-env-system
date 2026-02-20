package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cfec_soil_sensordata_test3")
public class SoilSensorData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String devid;
    private String devtyp;

    private BigDecimal nitrogen;
    private BigDecimal phosphorus;
    private BigDecimal potassium;
    private BigDecimal electrical;
    private BigDecimal ph;
    private BigDecimal humidity;
    private BigDecimal temperature;

    @TableField("battery_level")
    private BigDecimal batteryLevel;

    private BigDecimal longitude;
    private BigDecimal latitude;

    @TableField("collection_id")
    private String collectionId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

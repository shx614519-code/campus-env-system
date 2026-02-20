package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cfec_air_sensordata_test3")
public class AirSensorData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String devid;
    private String devtyp;

    private BigDecimal windDir;
    private BigDecimal windSpeed;
    private BigDecimal airTemp;
    private BigDecimal airHumi;
    private BigDecimal airPres;
    private BigDecimal airLigh;
    private BigDecimal uvInten;
    private BigDecimal uvIndex;
    private BigDecimal  airPM25;
    private BigDecimal airPM10;

    @TableField("battery_level")
    private BigDecimal batteryLevel;

    private BigDecimal longitude;
    private BigDecimal latitude;

    @TableField("collection_id")
    private String collectionId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

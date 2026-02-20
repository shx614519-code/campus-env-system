package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cfec_water_sensordata_test3")
public class WaterSensorData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String devid;
    private String devtyp;

    @TableField("water_turb")
    private BigDecimal waterTurb;

    @TableField("water_oxysat")
    private BigDecimal waterOxysat;

    @TableField("water_oxycon")
    private BigDecimal waterOxycon;

    @TableField("water_temp")
    private BigDecimal waterTemp;

    @TableField("water_ph")
    private BigDecimal waterPh;

    @TableField("water_ec")
    private BigDecimal waterEc;

    @TableField("water_sal")
    private BigDecimal waterSal;

    @TableField("water_tds")
    private BigDecimal waterTds;

    @TableField("battery_level")
    private BigDecimal batteryLevel;

    private BigDecimal longitude;
    private BigDecimal latitude;

    @TableField("collection_id")
    private String collectionId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

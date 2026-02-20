package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.entity.WaterSensorData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WaterSensorDataMapper extends BaseMapper<WaterSensorData> {
}

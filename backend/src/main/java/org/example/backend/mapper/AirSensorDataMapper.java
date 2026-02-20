package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.entity.AirSensorData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AirSensorDataMapper extends BaseMapper<AirSensorData> {
}

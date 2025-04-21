package com.example.pvdatafetch.mapper;

import com.example.pvdatafetch.entity.CommonData;
import com.yingfeng.api.YFNowval;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author piiaJet
 * @Create 2025/4/817:47
 */
@Mapper
public interface DataMapper {

    // 获取所有设备表名
    @Select("SELECT table_name FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() " +
            "AND (table_name LIKE 'inverter_%' OR table_name LIKE 'combiner_box_%' OR table_name LIKE 'pvfarm_%')")
    List<String> listDeviceTables();

    // 获取指定表的字段列表（排除datetime）
    @Select("SELECT column_name FROM information_schema.columns " +
            "WHERE table_schema = DATABASE() " +
            "AND table_name = #{tableName} " +
            "AND column_name != 'datetime'")
    List<String> listTableFields(@Param("tableName") String tableName);

    // 动态表数据插入（带时间对齐）
    @Insert({
            "<script>",
            "INSERT INTO ${tableName} (datetime, ",
            "  <foreach collection='values' item='item' separator=','>",
            "    `${item.cpid}`",
            "  </foreach>",
            ") VALUES (",
            "  #{alignTime}, ",
            "  <foreach collection='values' item='item' separator=','>",
            "    #{item.value.Value}",
            "  </foreach>",
            ")",
            "ON DUPLICATE KEY UPDATE ",
            "  <foreach collection='values' item='item' separator=','>",
            "    `${item.cpid}` = VALUES(`${item.cpid}`)",
            "  </foreach>",
            "</script>"
    })
    int insertAlignedData(@Param("tableName") String tableName,
                          @Param("alignTime") Date alignTime,
                          @Param("values") List<YFNowval> values);
}


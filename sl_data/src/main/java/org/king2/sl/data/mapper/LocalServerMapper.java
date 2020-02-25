package org.king2.sl.data.mapper;

import org.apache.ibatis.annotations.Select;
import org.king2.sl.common.pojo.SlServer;

import java.util.List;

/**
 * 本地服务Mapper
 */
public interface LocalServerMapper {


    @Select("SELECT * FROM sl_server")
    List<SlServer> getAll();
}


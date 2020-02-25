package org.king2.sl.common.pojo;

import org.king2.sl.common.pojo.SlUserTable;

public interface SlUserTableMapper {
    int deleteByPrimaryKey(Integer slUserId);

    int insert(SlUserTable record);

    int insertSelective(SlUserTable record);

    SlUserTable selectByPrimaryKey(Integer slUserId);

    int updateByPrimaryKeySelective(SlUserTable record);

    int updateByPrimaryKey(SlUserTable record);

}
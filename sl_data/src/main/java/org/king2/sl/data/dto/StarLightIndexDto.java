package org.king2.sl.data.dto;

import lombok.Data;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlDataType;

import java.util.List;

/**
 * 星光首页Dto
 */
@Data
public class StarLightIndexDto {

    /**
     * 历史记录
     */
    private List<SlBook> slBooks;
    /**
     * 消息的个数
     */
    private Integer messageSize;
    /**
     * 本网站的所有数据类型
     */
    private List<SlDataType> slDataTypes;


    // TODO 我的收益

    /**
     * 首页书刊
     */
    private List<SlBook> indexBooks;
}

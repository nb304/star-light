package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SlBookDto {

    /**
     * 原书本
     */
    private SlBook slBook;
    /**
     * 详细内容
     */
    private List<SlBookDe> slBookDes;

    /**
     * 如果为小说，那么这就是他的内容
     */
    private String content;

}

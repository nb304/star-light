package org.king2.sl.book.mapper;

import org.apache.ibatis.annotations.*;
import org.king2.sl.common.pojo.*;

import java.util.List;

/**
 * 本地的Book服务
 */
public interface LocalSlBookMapper {

    /**
     * 获取使用中的书本类型
     *
     * @return
     */
    @Select("SELECT * FROM sl_book_type WHERE sl_type_state = 1")
    List<SlBookType> getBookTypes();

    /**
     * 获取使用中的书本数据类型
     *
     * @return
     */
    @Select("SELECT * FROM sl_data_type WHERE sl_data_state = 1")
    List<SlDataType> getDataTypes();

    /**
     * 添加书本并返回自增ID
     *
     * @param slBook
     */
    @Insert("INSERT INTO sl_book (`sl_book_name` , `user_id` , `sl_data_type_id` , `sl_is_free` , " +
            "`sl_content` , `sl_cover_image` , `sl_max_order` , `sl_update_time` ) VALUES (#{st.slBookName}," +
            "#{st.userId},#{st.slDataTypeId},#{st.slIsFree},#{st.slContent},#{st.slCoverImage},#{st.slMaxOrder}" +
            ",#{st.slUpdateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "slBookId", keyColumn = "sl_book_id")
    void insert(@Param("st") SlBook slBook);


    /**
     * 批量添加Book对应的Type
     *
     * @param bookCorrData
     */
    @Insert("<script>" +
            "INSERT INTO sl_book_corr_data(`sl_data_id`,`sl_book_id`) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.slDataId},#{item.slBookId})" +
            "</foreach>" +
            "</script>")
    void batchBookCorrDataInsert(List<SlBookCorrData> bookCorrData);


    /**
     * 根据id查询书本信息
     *
     * @param bookId
     * @return
     */
    @Select("SELECT * FROM sl_book WHERE sl_book_id = #{bookId}")
    SlBook getBookById(Integer bookId);

    /**
     * 添加书本内容到SQL当中
     *
     * @param bookDes
     */
    @Insert("<script>" +
            "INSERT INTO sl_book_de (`sl_book_de_name`,`sl_boot_de_body` , `sl_parent_id` , " +
            "`sl_cost` , `sl_order` , `sl_create_time`) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.slBookDeName} , #{item.slBootDeBody} , #{item.slParentId} , " +
            "#{item.slCost} , #{item.slOrder} , #{item.slCreateTime}  )" +
            "</foreach>" +
            "</script>")
    void insertDe(List<SlBookDe> bookDes);

    /**
     * 更新Book的最大排序
     *
     * @param book
     */
    @Update("UPDATE sl_book SET sl_max_order = #{b.slMaxOrder} WHERE sl_book_id = #{b.slBookId}")
    void updateOrder(@Param("b") SlBook book);

    /**
     * 查询出所有的内容并升序
     *
     * @param bookId
     * @return
     */
    @Select("SELECT * FROM sl_book_de WHERE sl_parent_id = #{bookId} GROUP BY sl_order ASC")
    List<SlBookDe> getSlBookContentByBId(Integer bookId);
}

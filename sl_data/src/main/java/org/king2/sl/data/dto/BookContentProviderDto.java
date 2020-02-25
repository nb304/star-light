package org.king2.sl.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.king2.sl.common.pojo.SlUserTable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 书本内容的消费者DTO
 */
@Data
@AllArgsConstructor
public class BookContentProviderDto {

    private MultipartFile file;
    private SlUserTable slUserTable;
    private String cost;
    private InputStream inputStream;
}

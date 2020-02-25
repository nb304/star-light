package org.king2.sl.book.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.king2.sl.book.mapper")
public class BookConfig {
}

package org.king2.sl.data.service.range;

import org.king2.sl.common.pojo.SlBookDto;
import org.king2.sl.common.utils.SystemResult;
import org.springframework.stereotype.Component;

@Component
public class GetBookInfoManageServiceFallBack implements GetBookInfoManageService {

    @Override
    public SystemResult getBookInfoById(String bookId) {
        return new SystemResult(100, "服务器暂时压力过大,请稍后重试", new SlBookDto());
    }
}

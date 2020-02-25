package org.king2.sl.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlMessage {

    private Integer slMessageId;
    private String slMessageContent;
    private Integer slUserId;
    private String slReadState;
    private Date createTime;
    private Integer slSendUserId;
}

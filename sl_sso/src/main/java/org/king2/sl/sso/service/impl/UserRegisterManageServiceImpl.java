package org.king2.sl.sso.service.impl;

import cn.hutool.extra.mail.MailUtil;
import lombok.Data;
import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.state.UserState;
import org.king2.sl.common.utils.AESUtil;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.MD5Utils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.sso.mapper.LocalLsUserTableMapper;
import org.king2.sl.sso.pool.TaskPool;
import org.king2.sl.sso.service.UserRegisterManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 用户注册接口的默认实现类
 */
@Service("defaultRegisterImpl")
public class UserRegisterManageServiceImpl implements UserRegisterManageService {

    /**
     * 注入本地User的Mapper
     */
    @Autowired
    private LocalLsUserTableMapper localLsUserTableMapper;

    @Value("${defaultImage}")
    private String defaultImage;

    private static final Object REGISTER_LOCK = new Object();

    @Override
    @Transactional
    public SystemResult register(String content, HttpServletRequest request) throws Exception {

        SlUserTable slUserTable = null;
        synchronized (REGISTER_LOCK) {
            // 初始化的校验信息
            slUserTable = initCheck(content, request);
            // 添加信息
            localLsUserTableMapper.insertValue(slUserTable);
        }
        // 添加用户成功后的操作
        registerSuccessAfterHandler(slUserTable, request);
        return new SystemResult("注册成功");
    }

    /**
     * 添加用户成功后的操作
     *
     * @param slUserTable
     * @param request
     */
    private void registerSuccessAfterHandler(SlUserTable slUserTable, HttpServletRequest request) {
        // 删除Session中的值
        request.getSession().removeAttribute("register_session");
        // 往队列当中存入一条信息,发送邮件的信息
        TaskPool.TASK_POOL.execute(() -> {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("感谢您注册星光，那么接下来就来激活您的账号吧，点击以下连接即可完成激活，祝您使用愉快。（请尽快激活哦！）---");
            stringBuffer.append("http://192.168.0.105:8001/active?uName=" + slUserTable.getSlUserName() + "&email=" + slUserTable.getSlUserEmail());
            MailUtil.send(slUserTable.getSlUserEmail(), "账号激活", stringBuffer.toString(), true);
        });
    }

    /**
     * 初始化的校验信息
     *
     * @param content 加密中的信息
     * @param request 请求
     */
    private SlUserTable initCheck(String content, HttpServletRequest request) throws Exception {

        // 校验Session当中是否存在信息
        Object register_session = request.getSession().getAttribute("register_session");
        if (register_session == null) {
            throw new CheckValueException("注册失败，请刷新重试。");
        }

        // 解密信息
        String oldMsg = AESUtil.aesDecrypt(content);
        TempUserInfo slUserTable = JsonUtils.jsonToPojo(oldMsg.substring(1, oldMsg.length() - 1).replaceAll("\\\\", ""), TempUserInfo.class);

        // 校验用户名和密码和邮箱是否正确
        if (StringUtils.isEmpty(slUserTable.getUserName()) || StringUtils.isEmpty(slUserTable.getPassWord()) ||
                slUserTable.getUserName().length() > 11) {
            throw new CheckValueException("用户名或密码不能为空，且用户名在11位之间");
        } else if (StringUtils.isEmpty(slUserTable.getEmail())) {
            throw new CheckValueException("邮箱不能为空");
        } else if (!slUserTable.getEmail().contains("@") || !slUserTable.getEmail().contains(".") ||
                slUserTable.getEmail().length() > 200) {
            throw new CheckValueException("邮箱格式错误");
        }

        // 验证邮箱或者用户名是否存在
        SlUserTable isUserName = localLsUserTableMapper.getUserName(slUserTable.getUserName());
        if (isUserName != null) {
            throw new CheckValueException("用户名已存在");
        } else {
            SlUserTable email = localLsUserTableMapper.getEmail(slUserTable.getEmail());
            if (email != null && !(email.getSlUserState() + "").equals(UserState.NO_ACTIVE.getValue() + "")) {
                throw new CheckValueException("邮箱已存在");
            }
        }

        SlUserTable slUserTable1 = new SlUserTable(
                slUserTable.getUserName(),
                MD5Utils.md5(slUserTable.getPassWord()),
                defaultImage,
                slUserTable.getEmail(),
                new Date()
        );
        return slUserTable1;
    }

}

@Data
class TempUserInfo {
    private String userName;
    private String passWord;
    private String email;
}
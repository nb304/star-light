package org.king2.sl.sso.service.impl;

import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.state.UserState;
import org.king2.sl.sso.mapper.LocalLsUserTableMapper;
import org.king2.sl.sso.service.UserActiveManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户激活的服务
 */
@Service("defaultUserActiveService")
public class UserActiveManageServiceImpl implements UserActiveManageService {

    /* 用户激活的锁 */
    private static final Object ACTIVE_LOCK = new Object();

    @Autowired
    private LocalLsUserTableMapper localLsUserTableMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void active(String uName, String email) throws Exception {
        synchronized (ACTIVE_LOCK) {
            // 校验用户是否激活成功
            SlUserTable userName = localLsUserTableMapper.getUserName(uName);
            if (userName != null) {
                if (!(userName.getSlUserState() + "").equals(UserState.NO_ACTIVE.getValue() + "")) {
                    return;
                }
            }
            localLsUserTableMapper.active(uName);
            // 将其他没有激活的邮箱全部删除
            localLsUserTableMapper.deleteNoActive(email);
        }
    }
}

package org.king2.sl.data.controller;

import org.king2.sl.data.appoint.AddBookManageAppoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 添加数据Controller
 */
@Controller
public class AddBookManageController {


    /**
     * 注入Book的委托类
     */
    @Autowired
    private AddBookManageAppoint addBookManageAppoint;

    /**
     * 显示添加书本首页
     *
     * @return
     */
    @RequestMapping("/show/book")
    public String index(HttpServletRequest request) throws Exception {

        addBookManageAppoint.bookInit(request);
        return "BookManage/AddBook";
    }
}

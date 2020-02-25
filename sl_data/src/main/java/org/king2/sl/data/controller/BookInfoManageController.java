package org.king2.sl.data.controller;

import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.service.BookInfoManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Book详细信息Controller
 */
@Controller
@Validated
public class BookInfoManageController {

    /**
     * 注入默认的服务管理
     */
    @Autowired
    private BookInfoManageService defaultBookInfoService;


    /**
     * book内容的显示
     *
     * @return
     */
    @RequestMapping("/book/content")
    public String index(HttpServletRequest request, HttpServletResponse response,
                        @NotBlank(message = "bookId不能为空") @Pattern(regexp = "[0-9]{1,}", message = "BookId类型错误")
                                String bookId) throws Exception {

        SystemResult systemResult = defaultBookInfoService.initBookInfo(request, response, Integer.parseInt(bookId));
        request.setAttribute("result", systemResult);
        return "BookManage/LookBook";
    }

}

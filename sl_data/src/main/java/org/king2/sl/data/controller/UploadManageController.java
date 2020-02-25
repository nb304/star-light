package org.king2.sl.data.controller;

import org.king2.sl.common.annotations.CheckLogin;
import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlBookCorrData;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.AESUtil;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.MinioUtil;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.king2.sl.data.service.AddBookService;
import org.king2.sl.data.service.AddContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件上传管理Controller
 */
@Controller
@Validated
public class UploadManageController {

    /**
     * 图片服务器的地址、账号、密码
     */
    public static final String FILE_SERVER_URL = "http://39.105.41.2:9000/";
    public static final String FILE_USER_NAME = "king2username";
    public static final String FILE_PASS_WORD = "king2password";
    public static final String FILE_BUCKET_NAME = "king2-product-image";

    /**
     * 注入默认的添加BookService
     */
    @Autowired
    private AddBookService defaultAddBookService;

    @Autowired
    private AddContentService defaultAddContentService;


    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    @ResponseBody
    @CheckLogin
    public SystemResult upload(MultipartFile file, @NotBlank(message = "请将表单内容填写完成") String content,
                               @NotBlank(message = "类型不能为空") String typeId, HttpServletRequest request) throws Exception {


        // 校验文件信息
        String fileName = MinioUtil.checkUploadFile(file);
        // 创建Min IO对象
        MinioUtil util = new MinioUtil(FILE_SERVER_URL, FILE_USER_NAME, FILE_PASS_WORD,
                FILE_BUCKET_NAME);
        SystemResult uploadResult = util.uploadFile(file, fileName);

        // 上传图片完成，我们需要将Book的From表单填入到SQL中
        SystemResult systemResult = defaultAddBookService.addBook(content, uploadResult.getData().toString(), request,
                util, typeId);
        return systemResult;
    }

    /**
     * 添加默认的内容
     *
     * @param file    内容文件
     * @param bookId  bookId
     * @param cost    星币
     * @param request 请求
     * @return 返回添加的结果
     */
    @RequestMapping("/upload/content")
    @ResponseBody
    @CheckLogin
    public SystemResult uploadContent(MultipartFile file,
                                      @NotBlank(message = "bId不能为空") @Pattern(regexp = "[0-9]{1,}"
                                              , message = "bId类型错误") String bookId,
                                      @NotBlank(message = "每章收费的星币不能为空")
                                      @Pattern(regexp = "[0-9]{1,}", message = "星币只能为数字") String cost,
                                      HttpServletRequest request) {

        SystemResult systemResult = defaultAddContentService.addContent(Integer.parseInt(bookId), file,
                request, cost);
        return systemResult;
    }

}

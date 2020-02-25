package org.king2.sl.data.controller;

import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.dto.StarLightIndexDto;
import org.king2.sl.data.service.StarLightIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 星光首页Manage
 */
@Controller
public class StarLightManageController {

    @Autowired
    private StarLightIndexService defaultStarLightIndexService;


    /**
     * 星光首页的显示
     *
     * @return
     */
    @RequestMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) {

        SystemResult index = defaultStarLightIndexService.index(request, response);
        request.setAttribute("result", index);
        request.setAttribute("navType", "index");
        return "index";
    }

    /**
     * 显示用户须知页面
     *
     * @return
     */
    @RequestMapping("/mustKnow")
    public String mustKnow() {
        return "BookManage/MustKnow";
    }


    /**
     * 根据类型获取书本的Id
     *
     * @param typeId
     * @return
     */
    @RequestMapping("/getBookByTypeId")
    @ResponseBody
    public SystemResult getBookByTypeId(@NotBlank(message = "typeId不能为空")
                                        @Pattern(regexp = "[0-9]{1,}") String typeId) {
        StarLightIndexDto starLightIndexDto = new StarLightIndexDto();
        defaultStarLightIndexService.getIndexBooks(starLightIndexDto, BookCommandKey.XG_INDEX_REDIS_BOOK_KEY + typeId, Integer.parseInt(typeId));
        return new SystemResult(starLightIndexDto.getIndexBooks());
    }
}

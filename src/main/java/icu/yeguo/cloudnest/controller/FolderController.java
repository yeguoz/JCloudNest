package icu.yeguo.cloudnest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.constant.UserConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IFolderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "目录Controller")
@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private IFolderService folderService;

    @PostMapping
    public Response<Long> createFolder(HttpSession session,
                                       @RequestParam("path") String path,
                                       @RequestParam("name") String name) throws IOException {
        if (path == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "路径不能为空");
        Object object = session.getAttribute(UserConstant.USER_VO);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);
        Long id = folderService.createFolder(userVO.getId(), path,name);
        return Response.success(id);
    }
}

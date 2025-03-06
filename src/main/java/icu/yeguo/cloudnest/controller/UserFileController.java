package icu.yeguo.cloudnest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.constant.UserConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.vo.FileVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IUserFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "用户文件Controller")
@RestController
@RequestMapping("/userfile")
public class UserFileController {

    @Autowired
    private IUserFileService userFileService;

    @Operation(summary = "获取目录下文件", parameters = {
            @Parameter(name = "path", description = "路径", schema = @Schema(type = "String"))
    },
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Response.class)
                    )
                    )
            })
    @GetMapping
    public Response<FileVO> getUserFiles(HttpSession session,
                                         @RequestParam("path") String path) {
        if (path == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST,"路径不能为空");
        Object object = session.getAttribute(UserConstant.USER_VO);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);

        FileVO fileVO = userFileService.getUserFiles(userVO.getId(), path);
        return Response.success(fileVO);
    }

    @Operation(summary = "创建文件", parameters = {
            @Parameter(name = "path", description = "路径", schema = @Schema(type = "String")),
            @Parameter(name = "name", description = "文件名", schema = @Schema(type = "String"))
    },
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Response.class)
                    )
                    )
            })
    @PostMapping
    public Response<Long> createUserFile(HttpSession session,
                                         @RequestParam("path") String path,
                                         @RequestParam("name") String name) throws IOException {
        if (path == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST,"路径不能为空");
        Object object = session.getAttribute(UserConstant.USER_VO);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);

        Long id = userFileService.createUserFile(userVO.getId(), path,name);
        return Response.success(id);
    }
}

package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.entity.Share;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.vo.AdminShareFileVO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【cn_shares】的数据库操作Service
* @createDate 2025-03-20 10:15:57
*/
public interface IShareService extends IService<Share> {

    List<AdminShareFileVO> getSharedFilesByAdmin();
}

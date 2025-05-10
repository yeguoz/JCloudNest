package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.model.entity.Share;
import icu.yeguo.cloudnest.model.vo.AdminShareFileVO;
import icu.yeguo.cloudnest.service.IShareService;
import icu.yeguo.cloudnest.mapper.ShareMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【cn_shares】的数据库操作Service实现
 * @createDate 2025-03-20 10:15:57
 */
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share>
        implements IShareService {

    @Resource
    private ShareMapper shareMapper;

    @Override
    public List<AdminShareFileVO> getSharedFilesByAdmin() {
        return shareMapper.getSharedFilesByAdmin();
    }
}
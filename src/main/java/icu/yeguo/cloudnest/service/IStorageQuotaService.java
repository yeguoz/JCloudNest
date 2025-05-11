package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.vo.UserVO;

public interface IStorageQuotaService {
    void checkAvailableSpace(UserVO userVO, Long fileSize, UploadRequest uploadRequest);

    void updateUserStorage(UserVO userVO, Long size);
}
package com.ccg.futurerealization.request.service;

import com.ccg.futurerealization.request.model.VersionModel;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-17 下午5:07
 * @Version: 1.0
 */
public interface VersionService {

    @GET("FutureRealization/version.json")
    Call<VersionModel> getServerVersion();
}

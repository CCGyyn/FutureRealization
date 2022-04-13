package com.ccg.futurerealization.request.service;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-4-12 下午7:14
 * @Version: 1.0
 */
public interface DownloadFileService {

    /**
     * @param range
     * @param url
     * @return
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadFileByRxjava(@Header("Range") String range, @Url String url);

    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Header("Range") String range, @Url String url);
}

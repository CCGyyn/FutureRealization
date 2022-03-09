package com.ccg.futurerealization.request.service;

import com.ccg.futurerealization.Config;
import com.ccg.futurerealization.request.model.VersionModel;
import com.ccg.futurerealization.utils.LogUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-3-9 下午6:41
 * @Version: 1.0
 */
@Deprecated
public interface DemoService {

    /*
    *  @GET("boss/test")
    *  @Query("username") String username  -> boss/test?username={Query}
    *  @Path("Id") long id -> boss/{id}/test 或者用于 @GET("boss/test?id={id}")
    *  @Field 用与post请求
    *
    * @FormUrlEncoded
    * 用于修饰Field注解和FieldMap注解
    * 使用该注解,表示请求正文将使用表单网址编码。字段应该声明为参数，并用@Field注释或FieldMap注释。
    * 使用FormUrlEncoded注解的请求将具”application / x-www-form-urlencoded” MIME类型。
    * 字段名称和值将先进行UTF-8进行编码,再根据RFC-3986进行URI编码.
    * */

    @GET("boss/user/validlogin")
    Call<VersionModel> loginGet (@Query("username") String username, @Query("password") String password);

    @POST("boss/user/validlogin")
    @FormUrlEncoded
    Call<VersionModel> loginPost (@Field("username") String username, @Field("password") String password);

    @GET("boss/user/validlogin")
    Observable<VersionModel> loginGetRx (@Query("username") String username, @Query("password") String password);

    default void callTest() {
        DemoService versionService = getRetrofit().create(DemoService.class);
        Call<VersionModel> call = versionService.loginGet("111", "2222");
        call.enqueue(new Callback<VersionModel>() {
            @Override
            public void onResponse(Call<VersionModel> call, Response<VersionModel> response) {
                VersionModel body = response.body();
                String version_id = body.getVersion_id();
                LogUtils.i("version_id = " + version_id);
            }

            @Override
            public void onFailure(Call<VersionModel> call, Throwable t) {
                LogUtils.e(t.getMessage());
            }
        });
    }

    default void rxjavaTest() {
        DemoService versionService = getRetrofit().create(DemoService.class);
        Observable<VersionModel> observable = versionService.loginGetRx("111", "222");
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<VersionModel>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull VersionModel versionModel) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    default Retrofit getRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.CCG_GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}


package com.ccg.futurerealization.request;

import android.content.Context;

import com.ccg.futurerealization.Config;
import com.ccg.futurerealization.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-17 下午4:48
 * @Version: 1.0
 */
public class RetrofitHelper {

    private static volatile RetrofitHelper mRetrofitHelper;

    private static final String GITHUB_CER = "github.cer";


    private RetrofitHelper() {}

    public static RetrofitHelper getInstance() {
        if (mRetrofitHelper == null) {
            synchronized (RetrofitHelper.class) {
                if (mRetrofitHelper == null) {
                    mRetrofitHelper = new RetrofitHelper();
                }
            }
        }
        return mRetrofitHelper;
    }

    /*public Retrofit getRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.CCG_GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }*/

    /**
     * https忽略证书请求
     * @param context
     * @return
     */
    public Retrofit getRetrofitIgCer(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        try {
            clientBuilder.sslSocketFactory(getSSLSocketFactory()).hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.CCG_GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();
        return retrofit;
    }

    /**
     * https 需要证书
     * @param context
     * @return
     */
    public Retrofit getCCGRetrofit(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        try {
            setCertificates(clientBuilder,context.getAssets().open(GITHUB_CER));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.CCG_GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();
        return retrofit;
    }

    /**
     * 通过okhttpClient来设置证书
     * @param clientBuilder OKhttpClient.builder
     * @param certificates 读取证书的InputStream
     */
    public void setCertificates(OkHttpClient.Builder clientBuilder, InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory
                        .generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage());
                }
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }
    }

    private SSLSocketFactory getSSLSocketFactory() throws Exception {
        //创建一个不验证证书链的证书信任管理器。
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        }};

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts,
                new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        return sslContext
                .getSocketFactory();
    }
}

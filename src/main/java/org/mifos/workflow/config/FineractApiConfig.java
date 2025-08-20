package org.mifos.workflow.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import org.mifos.workflow.api.auth.AuthenticationApi;
import org.mifos.workflow.api.client.ClientsApi;
import org.mifos.workflow.api.loan.LoansApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.*;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/* * Configuration class for setting up Retrofit and OkHttpClient for Fineract API communication.
 * This class configures basic authentication, SSL trust settings, and logging for HTTP requests.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FineractApiConfig {

    private final WorkflowConfig properties;

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
    };

    private static final SSLContext trustAllSslContext;

    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SSL algorithm not available", e);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Failed to initialize SSL key management", e);
        }
    }

    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    @Bean
    public OkHttpClient okHttpClient() {
        log.info("Creating OkHttpClient with basic auth interceptor and SSL trust configuration");
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    String credentials = Credentials.basic(
                            properties.getFineract().getUsername(),
                            properties.getFineract().getPassword()
                    );
                    Request modifiedRequest = originalRequest.newBuilder()
                            .header("Authorization", credentials)
                            .header("Fineract-Platform-TenantId", properties.getFineract().getTenantId())
                            .build();
                    log.debug("Added Basic Auth header to request");
                    return chain.proceed(modifiedRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true);

        return builder.build();
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .create();
    }

    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient, Gson gson) {
        log.info("Creating Retrofit instance with base URL: {}", properties.getFineract().getBaseUrl());

        return new Retrofit.Builder()
                .baseUrl(properties.getFineract().getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Bean
    public AuthenticationApi authenticationApi(Retrofit retrofit) {
        log.info("Creating AuthenticationApi bean");
        return retrofit.create(AuthenticationApi.class);
    }

    @Bean
    public ClientsApi clientApi(Retrofit retrofit) {
        log.info("Creating ClientsApi bean");
        return retrofit.create(ClientsApi.class);
    }

    @Bean
    public LoansApi loansApi(Retrofit retrofit) {
        log.info("Creating LoansApi bean");
        return retrofit.create(LoansApi.class);
    }
}
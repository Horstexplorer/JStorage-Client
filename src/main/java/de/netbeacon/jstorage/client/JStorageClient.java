/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.jstorage.client;

import de.netbeacon.jstorage.client.executor.ScalingExecutor;
import de.netbeacon.jstorage.client.interceptor.RateLimitInterceptor;
import de.netbeacon.jstorage.client.request.JStorageRequestBuilder;
import de.netbeacon.jstorage.client.request.RequestType;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the client
 */
public class JStorageClient {

    private final OkHttpClient okHttpClient;
    private final ScalingExecutor scalingExecutor;

    private final String host;
    private final int port;

    private String userId;
    private String loginToken;
    private String password;

    /**
     * This creates a new instance of this class
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param userId of the user
     * @param password of the user
     * @param loginToken of the user
     * @param baseThreads number of threads always available
     * @param additionalThreads number of threads additionally available
     * @param maxWaitingTasks size of the queue on how many tasks can wait for execution
     * @param keepAliveTime unit of time after which the additional threads shut down after being idle
     * @param timeUnit timeunit of keepalive time
     * @param ignoreSSL ignores bad ssl certs (should only be used for dev operations during local tests)
     * @throws KeyManagementException on exception
     * @throws NoSuchAlgorithmException on exception
     */
    private JStorageClient(String host, int port, String userId, String password, String loginToken, int baseThreads, int additionalThreads, int maxWaitingTasks, int keepAliveTime, TimeUnit timeUnit, boolean ignoreSSL) throws KeyManagementException, NoSuchAlgorithmException {
        this.host = host;
        this.port = port;
        this.userId = userId;
        this.password = password;
        this.loginToken = loginToken;

        this.scalingExecutor = new ScalingExecutor(baseThreads, additionalThreads, maxWaitingTasks, keepAliveTime, timeUnit);
        this.okHttpClient = getOKHTTPClient((additionalThreads+baseThreads)*2, (additionalThreads+baseThreads)*2, ignoreSSL);
    }

    /**
     * Can be used to get an instance of this class with a basic settings preset
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param userId of the user
     * @param password of the user
     * @return JStorageClient
     */
    public static JStorageClient getClientSimple(String host, int port, String userId, String password){
        return getClient(host, port, userId, password, null, 4, 8, 1024, 10, TimeUnit.SECONDS);
    }

    /**
     * Can be used to get an instance of this class with a basic settings preset
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param loginToken of the user
     * @return JStorageClient
     */
    public static JStorageClient getClientSimple(String host, int port, String loginToken){
        return getClient(host, port, null, null, loginToken, 4, 8, 1024, 10, TimeUnit.SECONDS);
    }

    /**
     * Can be used to get an instance of this class with more advanced settings
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param userId of the user
     * @param password of the user
     * @param baseThreads number of threads always available
     * @param additionalThreads number of threads additionally available
     * @param maxWaitingTasks size of the queue on how many tasks can wait for execution
     * @return JStorageClient
     */
    public static JStorageClient getClientAdvanced(String host, int port, String userId, String password, int baseThreads, int additionalThreads, int maxWaitingTasks){
        return getClient(host, port, userId, password, null, baseThreads, additionalThreads, maxWaitingTasks, 10, TimeUnit.SECONDS);
    }

    /**
     * Can be used to get an instance of this class with more advanced settings
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param loginToken of the user
     * @param baseThreads number of threads always available
     * @param additionalThreads number of threads additionally available
     * @param maxWaitingTasks size of the queue on how many tasks can wait for execution
     * @return JStorageClient
     */
    public static JStorageClient getClientAdvanced(String host, int port, String loginToken, int baseThreads, int additionalThreads, int maxWaitingTasks){
        return getClient(host, port, null, null, loginToken, baseThreads, additionalThreads, maxWaitingTasks, 10, TimeUnit.SECONDS);
    }

    /**
     * Can be used to get an instance of this class with ssl checks disabled
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param userId of the user
     * @param password of the user
     * @return JStorageClient
     */
    public static JStorageClient getDevClient(String host, int port, String userId, String password){
        try{
            return new JStorageClient(host, port, userId, password, null, 2, 4, 128, 10, TimeUnit.SECONDS, true);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Can be used to get an instance of this class with ssl checks disabled
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param loginToken of the user
     * @return JStorageClient
     */
    public static JStorageClient getDevClient(String host, int port, String loginToken){
        try{
            return new JStorageClient(host, port, null, null, loginToken, 2, 4, 128, 10, TimeUnit.SECONDS, true);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Can be used to get a new client with a variety of setting available
     * <br>
     * returns null on exception
     *
     * @param host hostname of the jstorage server
     * @param port port of the jstorage server
     * @param userId of the user
     * @param password of the user
     * @param loginToken of the user
     * @param baseThreads number of threads always available
     * @param additionalThreads number of threads additionally available
     * @param maxWaitingTasks size of the queue on how many tasks can wait for execution
     * @param keepAliveTime unit of time after which the additional threads shut down after being idle
     * @param timeUnit timeunit of keepalive time
     * @return JStorageClient
     */
    public static JStorageClient getClient(String host, int port, String userId, String password, String loginToken, int baseThreads, int additionalThreads, int maxWaitingTasks, int keepAliveTime, TimeUnit timeUnit){
        try{
            return new JStorageClient(host, port, userId, password, loginToken, baseThreads, additionalThreads, maxWaitingTasks, keepAliveTime, timeUnit, false);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Can be used to shut down the client
     */
    public void shutdown(){
        try {
            this.scalingExecutor.shutdown();
            this.okHttpClient.dispatcher().executorService().shutdown();
            this.okHttpClient.connectionPool().evictAll();
            this.okHttpClient.cache().close();
        }catch (Exception ignore){}
    }

    /**
     * Used to change the lohin token of the current JStorageClient. Changes will only be applied to new Requests
     * @param loginToken logintoken
     */
    public void setLoginToken(String loginToken){
        this.loginToken = loginToken;
    }

    /**
     * Can be used to change the userID & password of the current JStorageClient. Changes will only be applied to new Requests
     * @param userId userid
     * @param password password
     */
    public void setLogin(String userId, String password){
        this.userId = userId;
        this.password = password;
    }

    /**
     * Can be used to build new requests
     * @param requestType of the request
     * @return JStorageRequestBuilder
     */
    public JStorageRequestBuilder newRequest(RequestType requestType){
        return new JStorageRequestBuilder(this, requestType);
    }

    /**
     * Used to get the OKHTTP client
     * @return OKHTTP client
     */
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * Used to get the executor
     * @return ScalingExecutor
     */
    public ScalingExecutor getScalingExecutor() {
        return scalingExecutor;
    }

    /**
     * Used to get the user id
     * @return userid
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Used to get the password
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Used to get the login token
     * @return login token
     */
    public String getLoginToken() {
        return loginToken;
    }

    /**
     * Used to get the hostname
     * @return hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Used to get the port of the host
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Used to get an okhttp client
     * @param maxRequests number of reqzests which could be sent concurrently
     * @param maxRequestsPerHost number of requests which could be send to the same host concurrently
     * @param sslDontCare ignores bad ssl certs (should only be used for dev operations during local tests)
     * @return OKHTTPClient
     * @throws NoSuchAlgorithmException on exception
     * @throws KeyManagementException on exception
     */
    private OkHttpClient getOKHTTPClient(int maxRequests, int maxRequestsPerHost, boolean sslDontCare) throws NoSuchAlgorithmException, KeyManagementException {

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);

        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(new RateLimitInterceptor()).dispatcher(dispatcher);

        if(sslDontCare){
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]).hostnameVerifier((hostname, session) -> true);
        }

        return builder.build();
    }
}

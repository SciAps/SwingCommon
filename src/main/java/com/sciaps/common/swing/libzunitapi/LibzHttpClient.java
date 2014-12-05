/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sciaps.common.swing.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jchen
 */
public class LibzHttpClient {

    private final Logger logger_ = LoggerFactory.getLogger(LibzHttpClient.class);

    public LibzHttpClient() {

    }

    public <T extends Object> T executePost(String URL, String jsonString, Class<T> classOfType) {
        logger_.info("Executing Post Request: " + URL);

        StringEntity stringEntity = getStringEntity(jsonString);

        if (getStringEntity(jsonString) != null) {
            HttpPost request = new HttpPost(URL);
            request.setEntity(stringEntity);
            return execute(request, classOfType);
        }

        return null;
    }

    public <T extends Object> T  executePut(String URL, String jsonString, Class<T> classOfType) {
        logger_.info("Executing Put Request: " + URL);

        StringEntity stringEntity = getStringEntity(jsonString);

        if (getStringEntity(jsonString) != null) {
            HttpPut request = new HttpPut(URL);
            request.setEntity(stringEntity);
            return execute(request, classOfType);
        }

        return null;
    }

    public <T extends Object> T  executeGet(String URL, Class<T> classOfType) {
        logger_.info("Executing Get Request: " + URL);

        HttpGet request = new HttpGet(URL);
        return execute(request, classOfType);
    }

    private StringEntity getStringEntity(String jsonString) {
        StringEntity entity = null;

        try {
            entity = new StringEntity(jsonString);
            entity.setContentType("Application/json");
        } catch (UnsupportedEncodingException ex) {
            logger_.error("UnsupportedEncodingException");
        }

        return entity;
    }

    private <T extends Object> T execute(HttpUriRequest request, Class<T> classTypeOfT) {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        HttpEntity responseEntity = null;
        
        T reponseObj = null;

        try {

            response = httpclient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                responseEntity = response.getEntity();
                JsonReader reader = new JsonReader(new InputStreamReader(responseEntity.getContent()));
                
                Gson gson = new GsonBuilder().create();
                reponseObj = gson.fromJson(reader, classTypeOfT);
                
                logger_.info("Request executed successfully.");

            } else {
                logger_.info("Request execution failed: " + response.getStatusLine());
            }

        } catch (IOException ex) {
            logger_.error("Request execution failed. ", ex);

        } finally {

            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException ex) {
                logger_.error("Response close failed.", ex);
            }

            try {
                httpclient.close();
            } catch (IOException ex) {
                logger_.error("Http client close failed", ex);
            }
        }

        return reponseObj;
    }
}

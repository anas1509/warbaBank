package com.warba.customer.customer_service.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpRequester {

	/**
	 * HttpRequest class is used to call external APIs, mainly used to communicate with the customer service
	 */
	
    private final RestTemplate restTemplate;
    private String url;
    private HttpMethod method;
    private HttpHeaders headers = new HttpHeaders();

    @Autowired
    public HttpRequester(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public HttpRequester url(String url) {
        this.url = url;
        return this;
    }

    public HttpRequester method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpRequester contentType(MediaType contentType) {
        headers.setContentType(contentType);
        return this;
    }

    public HttpRequester header(String key, String value) {
        headers.set(key, value);
        return this;
    }

    public <T> ResponseEntity<T> send(ParameterizedTypeReference<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, method, entity, responseType);
    }

    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<T> sendWithCatch(ParameterizedTypeReference<T> responseType) {
        ResponseEntity<T> responseEntity;
        try {
            responseEntity = send(responseType);
        } catch (RestClientResponseException e) {
            responseEntity = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body((T) e.getResponseBodyAsString());
        } catch (Exception e) { // fallback
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((T) e.getMessage());
        }

        return responseEntity;
    }
}

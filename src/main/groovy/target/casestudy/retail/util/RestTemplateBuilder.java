package target.casestudy.retail.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import target.casestudy.retail.exception.ProductServiceException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

@Slf4j
@Component
public class RestTemplateBuilder {

  @Autowired
  private ObjectMapper objectMapper;


  public RestTemplate buildRestTemplate() throws Exception {

    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http",  PlainSocketFactory.getSocketFactory(),80));
    schemeRegistry.register(new Scheme("https", new SSLSocketFactoryImpl(),443));

    PoolingClientConnectionManager httpClientFactory = new PoolingClientConnectionManager(schemeRegistry);
    httpClientFactory.setMaxTotal(1000);
    httpClientFactory.setDefaultMaxPerRoute(1000);

    DefaultHttpClient httpClient = new DefaultHttpClient(httpClientFactory);

    HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

    httpRequestFactory.setConnectTimeout(3000);
    httpRequestFactory.setReadTimeout(3000);
    RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
    restTemplate.setErrorHandler(new ProductErrorHandler());
    return restTemplate;

  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }


  private static class SSLSocketFactoryImpl extends SSLSocketFactory {
    public SSLSocketFactoryImpl() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
      super((arg0,arg1) -> true, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
  }

  private class ProductErrorHandler implements ResponseErrorHandler {
    private static final int ERROR = 400;
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
      if (clientHttpResponse.getStatusCode().value() >= ERROR) {
        return true;
      }
      return false;
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
      HttpStatus statusCode = clientHttpResponse.getStatusCode();
      String responseBody = "";

      if (clientHttpResponse.getBody() != null) {
        responseBody = IOUtils.toString(clientHttpResponse.getBody());
      }

      if (!StringUtils.isEmpty(responseBody)) {
        throw new ProductServiceException("Service Unavailable",responseBody);
      }

    }


  }
}

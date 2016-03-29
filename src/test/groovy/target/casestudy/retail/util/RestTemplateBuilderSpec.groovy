package target.casestudy.retail.util

import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestTemplateBuilderSpec extends Specification{

    RestTemplateBuilder restTemplateBuilder

    def setup(){
        restTemplateBuilder = new RestTemplateBuilder()
    }

    def "buildRestTemplate"(){

        setup:
        SchemeRegistry schemeRegistry = GroovyMock(SchemeRegistry)
        PoolingClientConnectionManager poolingClientConnectionManager = Mock(PoolingClientConnectionManager)
        DefaultHttpClient httpClient = Mock(DefaultHttpClient)
        HttpComponentsClientHttpRequestFactory requestFactory= Mock(HttpComponentsClientHttpRequestFactory)

        when:
        RestTemplate restTemplate = restTemplateBuilder.buildRestTemplate()

        then:
        assert restTemplate.getErrorHandler()


    }

//    def "ProductErrorHandler.hasError"(){
//
//        setup:
//       RestTemplateBuilder.
//        when:
//        RestTemplate restTemplate = restTemplateBuilder.buildRestTemplate()
//
//        then:
//        assert restTemplate
//
//    }


}

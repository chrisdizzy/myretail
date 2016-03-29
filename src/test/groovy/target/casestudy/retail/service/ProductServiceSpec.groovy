package target.casestudy.retail.service

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import target.casestudy.retail.domain.Price
import target.casestudy.retail.domain.SkuDetail

class ProductServiceSpec extends Specification {

    ProductService productService

    RestTemplate restTemplate

    MongoTemplate mongoTemplate

    def setup() {
        restTemplate = Mock(RestTemplate)
        mongoTemplate=Mock(MongoTemplate)
        productService = new ProductService(
                restTemplate: restTemplate,mongoTemplate: mongoTemplate,
                productApiUrl: "http://somedummy",
                reqUriFields: "fields",
                reqUriIdType: "type",
                reqUriKey: "kkk")
    }

    def "happy path - productService"() {
        setup:
        int productId = 13860428
        String productResponse = this.getClass().getResource('/mockresponse/product-api.json').text
        ResponseEntity responseEntity = Mock(ResponseEntity)
        Map map = ['fields': 'fields', 'id_type': 'type', 'key': 'kkk', 'productId': productId.toString()]
        SkuDetail skuDetailFromRepo = new SkuDetail(currentPrice: new Price(value: 12.11,currency:'USD' ))

        when:
        SkuDetail skuDetail = productService.getproductDetails(productId)


        then:
        1 * restTemplate.exchange("http://somedummy", HttpMethod.GET, _ as HttpEntity, String.class, map) >> responseEntity
        2 * responseEntity.getBody() >> productResponse
        1 * mongoTemplate.findOne(_ as org.springframework.data.mongodb.core.query.Query,SkuDetail.class) >>  skuDetailFromRepo

        skuDetail.skuName == 'The Big Lebowski [Blu-ray]'
        skuDetail.skuId == '13860428'
        skuDetail.currentPrice.value == 12.11
        skuDetail.currentPrice.currency == 'USD'

    }

    def "failure path - product not found in api "() {
        setup:
        int productId = 13868
        String productResponse = this.getClass().getResource('/mockresponse/product-api-failure.json').text
        ResponseEntity responseEntity = Mock(ResponseEntity)
        Map map = ['fields': 'fields', 'id_type': 'type', 'key': 'kkk', 'productId': productId.toString()]
        SkuDetail skuDetailFromRepo = new SkuDetail(currentPrice: new Price(value: 12.11,currency:'USD' ))
        when:
        SkuDetail skuDetail = productService.getproductDetails(productId)


        then:
        1 * restTemplate.exchange("http://somedummy", HttpMethod.GET, _ as HttpEntity, String.class, map) >>  responseEntity
        2 * responseEntity.getBody() >> productResponse
        1 * mongoTemplate.findOne(_ as org.springframework.data.mongodb.core.query.Query,SkuDetail.class) >>  skuDetailFromRepo
        skuDetail.errorResponse.errors[0].errorCode == 'Product Not Found'
        skuDetail.errorResponse.errors[0].errorMsg == 'Not valid product in system: This product ID does not represent a valid product::error message2'
        noExceptionThrown()
    }


    def "failure path - null response from db, respond a pojo with error msg "() {
        setup:
        int productId = 13860428
        String productResponse = this.getClass().getResource('/mockresponse/product-api.json').text
        ResponseEntity responseEntity = Mock(ResponseEntity)
        Map map = ['fields': 'fields', 'id_type': 'type', 'key': 'kkk', 'productId': productId.toString()]
        SkuDetail skuDetailFromRepo = new SkuDetail(currentPrice: new Price(value: 12.11,currency:'USD' ))
        when:
        SkuDetail skuDetail = productService.getproductDetails(productId)


        then:
        1 * restTemplate.exchange("http://somedummy", HttpMethod.GET, _ as HttpEntity, String.class, map) >>  responseEntity
        2 * responseEntity.getBody() >> productResponse
        1 * mongoTemplate.findOne(_ as org.springframework.data.mongodb.core.query.Query,SkuDetail.class) >>  null
        skuDetail.errorResponse.errors[0].errorCode == 'Internal Server Error'
        skuDetail.errorResponse.errors[0].errorMsg == 'Some of the DB Services are down, please try later'
        noExceptionThrown()

    }

    def "failure path - exception while calling api, respond a pojo with error msg "() {
        setup:
        int productId = 13860428
        String productResponse = this.getClass().getResource('/mockresponse/product-api.json').text
        ResponseEntity responseEntity = Mock(ResponseEntity)
        Map map = ['fields': 'fields', 'id_type': 'type', 'key': 'kkk', 'productId': productId.toString()]
        SkuDetail skuDetailFromRepo = new SkuDetail(currentPrice: new Price(value: 12.11,currency:'USD' ))
        when:
        SkuDetail skuDetail = productService.getproductDetails(productId)


        then:
        1 * restTemplate.exchange("http://somedummy", HttpMethod.GET, _ as HttpEntity, String.class, map) >>    {throw new RuntimeException("exception in calling service") }
        0 * responseEntity.getBody()
        1 * mongoTemplate.findOne(_ as org.springframework.data.mongodb.core.query.Query,SkuDetail.class) >>  null
        skuDetail.errorResponse.errors[0].errorCode == 'Internal Server Error'
        skuDetail.errorResponse.errors[0].errorMsg == 'Exception from base services, please try later'
        noExceptionThrown()

    }


    def "failure path - exception while calling db , respond a pojo with error msg"() {
        setup:
        int productId = 13860428
        String productResponse = this.getClass().getResource('/mockresponse/product-api.json').text
        ResponseEntity responseEntity = Mock(ResponseEntity)
        Map map = ['fields': 'fields', 'id_type': 'type', 'key': 'kkk', 'productId': productId.toString()]
        SkuDetail skuDetailFromRepo = new SkuDetail(currentPrice: new Price(value: 12.11,currency:'USD' ))
        when:
        SkuDetail skuDetail = productService.getproductDetails(productId)


        then:
        1 * restTemplate.exchange("http://somedummy", HttpMethod.GET, _ as HttpEntity, String.class, map) >>    responseEntity
        2 * responseEntity.getBody() >> productResponse
        1 * mongoTemplate.findOne(_ as org.springframework.data.mongodb.core.query.Query,SkuDetail.class) >>   {throw new RuntimeException("exception in calling db") }
        skuDetail.errorResponse.errors[0].errorCode == 'Internal Server Error'
        skuDetail.errorResponse.errors[0].errorMsg == 'Exception from base services, please try later'
        noExceptionThrown()

    }

    def "happy path - updatesku"() {
        setup:
        int productId = 13860428
        SkuDetail skuDetail = new SkuDetail(skuId:13860428,skuName: 'The Big Lebowski [Blu-ray]', currentPrice:  new Price(value:13.59, currency:  'USD'))

        when:
        productService.updateSku(skuDetail)

        then:
        1 * mongoTemplate.findAndModify(_ as org.springframework.data.mongodb.core.query.Query,
                _ as org.springframework.data.mongodb.core.query.Update,
                _ as org.springframework.data.mongodb.core.FindAndModifyOptions,
               SkuDetail.class) >> skuDetail
        skuDetail.currentPrice.value == 13.59
        skuDetail.currentPrice.currency == 'USD'

    }



}
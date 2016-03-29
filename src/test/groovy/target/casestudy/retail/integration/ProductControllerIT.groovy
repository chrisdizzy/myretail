package target.casestudy.retail.integration

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification
import target.casestudy.retail.WebappInit
import target.casestudy.retail.service.ProductService
import target.casestudy.retail.util.RestTemplateBuilder

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@ContextConfiguration(loader = SpringApplicationContextLoader, classes= WebappInit.class)
@WebAppConfiguration
class ProductControllerIT extends Specification{

    MockMvc mockMvc

    @Autowired
    WebApplicationContext webApplicationContext

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    ProductService productService

    @Autowired
    RestTemplateBuilder restTemplateBuilder

    def 'failure - get on a invalid product id'(){

       setup:
       mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        when:
        MockHttpServletResponse response = mockMvc.perform(get("/products/13868").secure(true)).andReturn().getResponse()

        then:
        def responseBody = new JsonSlurper().parse(response.contentAsByteArray)
        responseBody.errorResponse.errors.size == 1
        responseBody.errorResponse.errors[0].errorMsg =='Not valid product in system: This product ID does not represent a valid product'

    }


    def 'success - get on a valid product id'(){

        setup:
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        when:
        MockHttpServletResponse response = mockMvc.perform(get("/products/13860428").secure(true)).andReturn().getResponse()

        then:
        def responseBody = new JsonSlurper().parse(response.contentAsByteArray)
        responseBody.id == '13860428'
        responseBody.name == 'The Big Lebowski [Blu-ray]'
        responseBody.currentPrice.currency == 'USD'

    }

    def 'success - put on a valid product id - price only update'(){

        setup:
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        String payLoad ='{"currentPrice": {"value": 13.60,"currency": "USD"},"id": "13860428","name": "some new name which wont update"}'
        when:
        MockHttpServletResponse response  = mockMvc.perform(put('/products/13860428')
                .content(payLoad)
                .contentType(MediaType.APPLICATION_JSON).secure(true)).andReturn().getResponse()

        then:
        def responseBody = new JsonSlurper().parse(response.contentAsByteArray)
        responseBody.id == '13860428'
        responseBody.name == 'The Big Lebowski [Blu-ray]'
        responseBody.currentPrice.value == 13.6
        responseBody.currentPrice.currency == 'USD'
    }


}

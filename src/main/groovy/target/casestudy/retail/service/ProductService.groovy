package target.casestudy.retail.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import target.casestudy.retail.contract.ErrorBody
import target.casestudy.retail.contract.ErrorResponse
import target.casestudy.retail.domain.Price
import target.casestudy.retail.domain.SkuDetail
import target.casestudy.retail.exception.ProductServiceException
import target.casestudy.retail.util.RestTemplateBuilder
import target.casestudy.retail.util.Threadable

import javax.annotation.PostConstruct
import java.util.concurrent.Future

@Slf4j
@Component
class ProductService implements Threadable {

    String applicationIdHeader

    String applicationId

    @Autowired
    RestTemplateBuilder restTemplateBuilder

    @Value('${target.productApiUrl}')
    String productApiUrl

    @Value('${target.productApiUrl.requestUri.fields}')
    String reqUriFields

    @Value('${target.productApiUrl.requestUri.id_type}')
    String reqUriIdType

    @Value('${target.productApiUrl.requestUri.key}')
    String reqUriKey

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MongoTemplate mongoTemplate

    RestTemplate restTemplate


   @PostConstruct
   public void getRestTemplate(){
       restTemplate = restTemplateBuilder.buildRestTemplate()
   }

    public SkuDetail getproductDetails(Integer productId) {
        log.info("action=getproductDetails::productId:${productId}")
        Future<SkuDetail> productMapFromApi = null
        Future<SkuDetail> productMapFromDB = null
        SkuDetail skuDetail = null
        long startTime = System.currentTimeMillis()
        try{
            // execute api call and db call parallely as an async process
        GParsPool.withPool {
            productMapFromApi = { getproductDetailsFromApi(productId) }.callAsync()
            productMapFromDB = { getproductDetailsFromRepository(productId) }.callAsync()
            unwrapExecutionException {
                skuDetail = productMapFromApi.get()
            }
           // if no error from api call, then parse db values
            if(!skuDetail.errorResponse)
            unwrapExecutionException {
                SkuDetail skuFromRepo = productMapFromDB.get()
                skuDetail?.currentPrice = new Price(value: skuFromRepo?.currentPrice?.value, currency: skuFromRepo?.currentPrice?.currency)
                if(!skuDetail?.currentPrice?.value || !skuDetail?.currentPrice?.currency){
                    skuDetail = new SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Internal Server Error",errorMsg: "Some of the DB Services are down, please try later")]))
                }
            }
        }
            log.info("action=getproductDetails exit ::productId:${productId}::time taken in ms:${ System.currentTimeMillis() - startTime}::skudetail:${skuDetail}")
    }catch(Exception e) {
            if(e.getCause() instanceof ProductServiceException){
                throw e
            }
            log.error("Exception occured::action=getproductDetails()::exception:${e.message}",e)
            skuDetail = new SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Internal Server Error",errorMsg: "Exception from base services, please try later")]))
            log.info("action=getproductDetails exception exit::productId:${productId}::skudetails:${skuDetail}")
        }

        return skuDetail
    }


    public SkuDetail getproductDetailsFromApi(Integer productId){
        log.info("action=getproductDetailsFromApi::productId:${productId}")
        HttpEntity httpEntity = new HttpEntity(getHeaders())
        Map map =["fields":reqUriFields,"id_type":reqUriIdType,"key":reqUriKey,"productId":productId.toString()]
        ResponseEntity responseEntity =  restTemplate.exchange(productApiUrl, HttpMethod.GET, httpEntity, String.class,map)
        SkuDetail skuDetail = null
        if(responseEntity?.getBody()) {
            println("inside responseEntity?.getBody() " )
            def result = new JsonSlurper().parseText(responseEntity?.getBody())
            def item = result?.product_composite_response?.items?.find { it.identifier*.id?.contains(productId as String) }
            if (item && !item?.errors) {
                skuDetail = new SkuDetail()
                skuDetail.skuId = productId
                skuDetail.skuName = item?.online_description?.value
            }else{
                def errorList = item?.errors
                def messages = errorList*.message
                skuDetail = new  SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Product Not Found",errorMsg: messages.join('::'))]))

            }
        }
        log.info("action=getproductDetailsFromApi exit::productId:${productId}::skudetail from API:${skuDetail}")
        return skuDetail;

    }

    public SkuDetail getproductDetailsFromRepository(Integer productId){
        log.info("action=getproductDetailsFromRepository::productId:${productId}")
        Query searchUserQuery = new Query(Criteria.where("skuId").is(productId.toString()))
        SkuDetail skuFromRepo = mongoTemplate.findOne(searchUserQuery,SkuDetail.class)
        log.info("action=getproductDetailsFromRepository exit::productId:${productId}::skudetail from DB:${skuFromRepo}")
        return skuFromRepo
    }

    private HttpHeaders getHeaders(){
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.add("Content-Type",MediaType.APPLICATION_JSON_VALUE)
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE)
        return httpHeaders
    }

    public SkuDetail updateSku(SkuDetail skuDetail){
        log.info("action=updateSku::request skuDetail:${skuDetail}")
        Query query = new Query(Criteria.where("skuId").is(skuDetail.skuId));
        Update update = new   Update()
        update.set("currentPrice",skuDetail.currentPrice)
        SkuDetail updatedSku= mongoTemplate.findAndModify(query,update,new FindAndModifyOptions().returnNew(true), SkuDetail.class)
        log.info("action=updateSku exit::updated skuDetail from DB skuDetail:${updatedSku}")
        return updatedSku
    }

    public SkuDetail insertSku(SkuDetail skuDetail){
        log.info("action=insertSku::request skuDetail:${skuDetail}")
        mongoTemplate.save(skuDetail)
        Query searchUserQuery = new Query(Criteria.where("skuId").is(skuDetail.skuId))
        SkuDetail savedSku = mongoTemplate.findOne(searchUserQuery,SkuDetail.class)
        log.info("action=insertSku exit ::savedSku skuDetail from DB:${skuDetail}")
        return savedSku
    }


}

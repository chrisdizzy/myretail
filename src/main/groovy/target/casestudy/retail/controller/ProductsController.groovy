package target.casestudy.retail.controller

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import target.casestudy.retail.contract.ErrorBody
import target.casestudy.retail.contract.ErrorResponse
import target.casestudy.retail.domain.SkuDetail
import target.casestudy.retail.service.ProductService

import static org.springframework.http.HttpStatus.OK

@Slf4j
@RestController
class ProductsController {

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    ProductService productService


    @ResponseBody
    @ResponseStatus(value = OK)
    @RequestMapping(method = RequestMethod.GET, value = '/products/{productId}', produces =  MediaType.APPLICATION_JSON_VALUE)
    public SkuDetail getSku(@PathVariable("productId") Integer productId ){
        log.info("action=getSku::productid:${productId}")
           return productService.getproductDetails(productId)

    }

    @ResponseBody
    @ResponseStatus(value = OK)
    @RequestMapping(method = RequestMethod.PUT, value = '/products/{productId}', consumes =MediaType.APPLICATION_JSON_VALUE, produces =  MediaType.APPLICATION_JSON_VALUE)
    public SkuDetail updateSku(@RequestBody SkuDetail skuDetail, @PathVariable("productId") Integer productId  ){
        log.info("action=updateSku::skuDetail:${skuDetail}")

        if(productId != skuDetail.skuId as Integer){
            return new  SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Product Not Found",errorMsg:'Product id in body does not match with Product Id being requested')]))
        }
        SkuDetail updatedSkuDetail = productService.updateSku(skuDetail)
        if(!updatedSkuDetail){
            updatedSkuDetail = new  SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Product Not Found",errorMsg:'Product id:' +skuDetail.skuId +' not found, search with a different Product id')]))
        }
            return  updatedSkuDetail

    }


    @ResponseBody
    @ResponseStatus(value = OK)
    @RequestMapping(method = RequestMethod.POST, value = '/products/{productId}', consumes =MediaType.APPLICATION_JSON_VALUE, produces =  MediaType.APPLICATION_JSON_VALUE)
    public SkuDetail insertSku(@RequestBody SkuDetail skuDetail, @PathVariable("productId") Integer productId  ){
        log.info("action=insertSku::skuDetail:${skuDetail}")

        if(productId != skuDetail.skuId as Integer){
            return new  SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Product Id Mismatch",errorMsg:'Product id in body does not match with Product Id being requested')]))
        }
        SkuDetail savedSkuDetail = productService.insertSku(skuDetail)
        if(!savedSkuDetail){
            savedSkuDetail = new  SkuDetail(errorResponse: new ErrorResponse(errors : [new ErrorBody(errorCode: "Product Not Found",errorMsg:'Product id:' +skuDetail.skuId +' not saved')]))
        }
        return  savedSkuDetail

    }
}

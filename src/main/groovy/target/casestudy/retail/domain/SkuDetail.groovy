package target.casestudy.retail.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import groovy.transform.ToString
import org.springframework.data.annotation.Id
import target.casestudy.retail.contract.ErrorResponse

@JsonPropertyOrder(["skuId","skuName","currentPrice"])
@ToString
class SkuDetail {
    @Id
    @JsonProperty("id")
    String skuId

    @JsonProperty("name")
    String skuName

    Price currentPrice

    ErrorResponse errorResponse


}

package target.casestudy.retail.config

import com.mongodb.MongoClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory

@Configuration
class DbConfig {
    @Value('${target.mongo.db.name}')
    private String mongo_dbName
    @Value('${target.mongo.host}')
    private String mongo_host
    @Value('${target.mongo.port}')
    private String mongo_port

    @Bean
    MongoDbFactory mongoDbFactory(){
        MongoDbFactory  mongoDbFactory = new SimpleMongoDbFactory(new MongoClient(mongo_host,Integer.parseInt(mongo_port)),mongo_dbName)
         return mongoDbFactory
    }

    @Bean
    MongoTemplate mongoTemplate(){
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        return mongoTemplate
    }


}

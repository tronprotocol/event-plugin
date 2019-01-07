package org.tron.orm.mongo.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.tron.orm.mongo.EventLogMongoDao;
import org.tron.orm.mongo.entity.EventLogEntity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Repository("eventLogMongoDaoImpl")
public class EventLogMongoDaoImpl<T> implements EventLogMongoDao<T> {

  private static Logger log = LoggerFactory.getLogger(EventLogMongoDaoImpl.class);

  protected Class<T> entityClass;

  @Autowired
  private MongoTemplate mongoTemplate;

  public EventLogMongoDaoImpl() {
    Type superType = getClass().getGenericSuperclass();
    if (superType instanceof Class) {
      entityClass = (Class<T>) superType;
    }
    if (superType instanceof ParameterizedType) {
      entityClass = (Class<T>) ((ParameterizedType) superType).getActualTypeArguments()[0];
    }


  }

  private boolean checkCollection(String collection){
    return mongoTemplate.collectionExists(collection);
  }

  private void createIndex(String index, String collection){
    mongoTemplate.indexOps(collection).ensureIndex(new Index().on(index, Sort.Direction.DESC));
  }

  @Override
  public void insert(Object object, String collectionName) {
    boolean collectionExists = this.checkCollection(collectionName);
    mongoTemplate.insert(object, collectionName);
    if (!collectionExists) {
      this.createIndex("event_name", collectionName);
      this.createIndex("transaction_id", collectionName);
      this.createIndex("block_timestamp", collectionName);
      this.createIndex("block_number", collectionName);
    }
  }

  @Override
  public List<EventLogEntity> findAll(String contractAddress, String collectionName) {

    Query query = new Query(Criteria.where("contract_address").is(contractAddress));
    return mongoTemplate.find(query, EventLogEntity.class, collectionName);

  }

  @Override
  public EventLogEntity findOne(String contractAddress, String collectionName) {

    Query query = new Query(Criteria.where("contract_address").is(contractAddress));
    return mongoTemplate.findOne(query, EventLogEntity.class, collectionName);
  }

  @Override
  public List<EventLogEntity> findAll(String contractAddress, String eventName, String collectionName) {
    Query query = new Query(Criteria.where("contract_address").is(contractAddress).and("event_name").is(eventName));
    return mongoTemplate.find(query, EventLogEntity.class, collectionName);
  }

  @Override
  public EventLogEntity findOne(String contractAddress, String eventName, String collectionName) {
    Query query = new Query(Criteria.where("contract_address").is(contractAddress).and("event_name").is(eventName));
    return mongoTemplate.findOne(query, EventLogEntity.class, collectionName);
  }

  @Override
  public List<EventLogEntity> findAll(String contractAddress, String eventName, long blockNumber, String collectionName) {
    Query query = new Query(Criteria.where("contract_address").is(contractAddress).and("event_name").is(eventName).and("block_number").is(blockNumber));
    return mongoTemplate.find(query, EventLogEntity.class, collectionName);
  }

  @Override
  public EventLogEntity findOne(String contractAddress, String eventName, long blockNumber, String collectionName) {
    Query query = new Query(Criteria.where("contract_address").is(contractAddress).and("event_name").is(eventName).and("block_number").is(blockNumber));
    return mongoTemplate.findOne(query, EventLogEntity.class, collectionName);
  }

  @Override
  public List<EventLogEntity> findAllByTransactionId(String transactionId, String collectionName) {
    Query query = new Query(Criteria.where("transaction_id").is(transactionId));
    return mongoTemplate.find(query, EventLogEntity.class, collectionName);
  }

  @Override
  public EventLogEntity findOneByTransactionId(String transactionId, String collectionName) {
    Query query = new Query(Criteria.where("transaction_id").is(transactionId));
    return mongoTemplate.findOne(query, EventLogEntity.class, collectionName);
  }

}

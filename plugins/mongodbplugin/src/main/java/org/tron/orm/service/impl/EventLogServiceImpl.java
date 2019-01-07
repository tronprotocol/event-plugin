package org.tron.orm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;
import org.tron.eventplugin.Constant;
import org.tron.orm.mongo.entity.EventLogEntity;
import org.tron.orm.mongo.impl.EventLogMongoDaoImpl;
import org.tron.orm.service.EventLogService;

import java.util.List;

@Service
public class EventLogServiceImpl implements EventLogService {

  @Autowired
  private EventLogMongoDaoImpl eventLogMongoDao;

  public EventLogServiceImpl(){
    eventLogMongoDao = new EventLogMongoDaoImpl();
  }

  private static final String COLLECTION_EVENT_LOG_CENTER = "eventLog";

  @Override
  public void insertBlockTrigger(BlockLogTrigger trigger) {
    eventLogMongoDao.insertBlockTrigger(trigger, Constant.BLOCK_TRIGGER_NAME);
  }

  @Override
  public void insertTransactionTrigger(TransactionLogTrigger trigger) {
    eventLogMongoDao.insertTransactionTrigger(trigger, Constant.TRANSACTION_TRIGGER_NAME);
  }

  @Override
  public void insertContractLogTrigger(ContractLogTrigger trigger) {
    eventLogMongoDao.insertContractLogTrigger(trigger, Constant.CONTRACTLOG_TRIGGER_NAME);
  }

  @Override
  public void insertContractEventTrigger(ContractEventTrigger trigger) {
    eventLogMongoDao.insertContractEventTrigger(trigger, Constant.CONTRACTEVENT_TRIGGER_NAME);
  }

  @Override
  public List<EventLogEntity> findAll(String contractAddress) {
    return eventLogMongoDao.findAll(contractAddress, COLLECTION_EVENT_LOG_CENTER);
  }

  @Override
  public EventLogEntity findOne(String contractAddress) {
    return eventLogMongoDao.findOne(contractAddress, COLLECTION_EVENT_LOG_CENTER);
  }

  @Override
  public List<EventLogEntity> findAll(String contractAddress, String eventName) {
    return eventLogMongoDao.findAll(contractAddress, eventName, COLLECTION_EVENT_LOG_CENTER);
  }

  @Override
  public EventLogEntity findOne(String contractAddress, String eventName) {
    return eventLogMongoDao.findOne(contractAddress, eventName, COLLECTION_EVENT_LOG_CENTER);
  }

  @Override
  public List<EventLogEntity> findAll(String contractAddress, String eventName, long blockNumber) {
    return eventLogMongoDao.findAll(contractAddress, eventName, blockNumber, COLLECTION_EVENT_LOG_CENTER);
  }

  @Override
  public EventLogEntity findOne(String contractAddress, String eventName, long blockNumber) {
    return eventLogMongoDao.findOne(contractAddress, eventName, blockNumber, COLLECTION_EVENT_LOG_CENTER);
  }


}

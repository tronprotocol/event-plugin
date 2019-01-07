package org.tron.orm.mongo;

import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;
import org.tron.orm.mongo.entity.EventLogEntity;

import java.util.List;


public interface EventLogMongoDao{
  public List<EventLogEntity> findAll(String contractAddress, String collectionName);

  public EventLogEntity findOne(String contractAddress, String collectionName);

  public List<EventLogEntity> findAll(String contractAddress, String entryName, String collectionName);

  public EventLogEntity findOne(String contractAddress, String entryName, String collectionName);

  public List<EventLogEntity> findAll(String contractAddress, String entryName, long blockNumber, String collectionName);

  public EventLogEntity findOne(String contractAddress, String entryName, long blockNumber, String collectionName);

  public List<EventLogEntity> findAllByTransactionId(String transactionId, String collectionName);

  public EventLogEntity findOneByTransactionId(String transactionId, String collectionName);

  public void insertBlockTrigger(BlockLogTrigger object, String collectionName);

  public void insertTransactionTrigger(TransactionLogTrigger object, String collectionName);

  public void insertContractLogTrigger(ContractLogTrigger object, String collectionName);

  public void insertContractEventTrigger(ContractEventTrigger object, String collectionName);
}

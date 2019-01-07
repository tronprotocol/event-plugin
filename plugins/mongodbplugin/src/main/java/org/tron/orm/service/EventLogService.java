package org.tron.orm.service;

import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;
import org.tron.orm.mongo.entity.EventLogEntity;

import java.util.List;

public interface EventLogService {

  public void insertBlockTrigger(BlockLogTrigger trigger);
  public void insertTransactionTrigger(TransactionLogTrigger trigger);
  public void insertContractLogTrigger(ContractLogTrigger trigger);
  public void insertContractEventTrigger(ContractEventTrigger trigger);

  public List<EventLogEntity> findAll(String contractAddressHexString);

  public EventLogEntity findOne(String contractAddressHexString);

  public List<EventLogEntity> findAll(String contractAddressHexString, String entryName);

  public EventLogEntity findOne(String contractAddressHexString, String entryName);

  public List<EventLogEntity> findAll(String contractAddressHexString, String entryName, long blockNumber);

  public EventLogEntity findOne(String contractAddressHexString, String entryName, long blockNumber);
}

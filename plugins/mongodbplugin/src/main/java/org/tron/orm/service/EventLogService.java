package org.tron.orm.service;

import org.tron.orm.mongo.entity.EventLogEntity;

import java.util.List;

public interface EventLogService {

  public void insertEventLog(EventLogEntity eventLog);

  public void insertEventLogCollection(EventLogEntity eventLog, String collectionName);

  public List<EventLogEntity> findAll(String contractAddressHexString);

  public EventLogEntity findOne(String contractAddressHexString);

  public List<EventLogEntity> findAll(String contractAddressHexString, String entryName);

  public EventLogEntity findOne(String contractAddressHexString, String entryName);

  public List<EventLogEntity> findAll(String contractAddressHexString, String entryName, long blockNumber);

  public EventLogEntity findOne(String contractAddressHexString, String entryName, long blockNumber);
}

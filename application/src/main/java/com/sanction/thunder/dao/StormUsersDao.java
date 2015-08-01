package com.sanction.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

import javax.inject.Inject;

public class StormUsersDao {
  private final Table table;

  @Inject
  public StormUsersDao(DynamoDB dynamo) {
    this.table = dynamo.getTable("thunder-users-test");
  }

}

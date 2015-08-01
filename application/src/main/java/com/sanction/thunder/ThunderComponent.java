package com.sanction.thunder;

import com.sanction.thunder.dao.DaoModule;
import com.sanction.thunder.dynamodb.DynamoDbModule;
import com.sanction.thunder.resources.UserResource;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {DaoModule.class, DynamoDbModule.class, ThunderModule.class})
public interface ThunderComponent {

  UserResource getUserResource();
}

package com.sanction.thunder;

import com.sanction.thunder.resources.UserResource;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component
public interface ThunderComponent {

  UserResource getUserResource();
}

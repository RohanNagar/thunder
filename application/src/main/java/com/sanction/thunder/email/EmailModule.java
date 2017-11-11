package com.sanction.thunder.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class EmailModule {

  /**
   * Supplies singleton AmazonSimpleEmailService instance for dependency injection.
   *
   * @return An AmazonSimpleEmailService instance.
   */
  @Singleton
  @Provides
  public AmazonSimpleEmailService provideAmazonSimpleEmailService() {
    return AmazonSimpleEmailServiceClientBuilder
        .standard()
        .withRegion("us-east-1")
        .build();
  }

  @Singleton
  @Provides
  public EmailService provideEmailService(AmazonSimpleEmailService emailService) {
    return new EmailService(emailService);
  }

}

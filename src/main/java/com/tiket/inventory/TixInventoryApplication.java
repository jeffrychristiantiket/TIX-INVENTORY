package com.tiket.inventory;

import com.tiket.inventory.service.test.GetHotelRedirectionsByTargetPublicId;
import com.tiket.inventory.service.test.PublishHotelRedirectionByMongoId;
import com.tiket.inventory.service.test.PublishHotelToB2c;
import com.tiket.inventory.service.test.ReplaceHotelPublicId;
import com.tiket.inventory.service.test.SoftDeleteHotelRedirectionsByMongoId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TixInventoryApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(TixInventoryApplication.class, args);
  }

  @Autowired
  GetHotelRedirectionsByTargetPublicId getHotelRedirectionsByTargetPublicId;

  @Autowired
  ReplaceHotelPublicId replaceHotelPublicId;

  @Autowired
  PublishHotelToB2c publishHotelToB2c;

  @Autowired
  SoftDeleteHotelRedirectionsByMongoId softDeleteHotelRedirectionsByMongoId;

  @Autowired
  PublishHotelRedirectionByMongoId publishHotelRedirectionByMongoId;

  @Override
  public void run(String... args) {
//    getHotelRedirectionsByTargetPublicId.getHotelRedirectionsByTargetPublicIdTest();
//    replaceHotelPublicId.replaceHotelPublicIdTest();
//    publishHotelToB2c.publishHotelToB2cTest();
//    softDeleteHotelRedirectionsByMongoId.softDeleteHotelRedirectionsByMongoIdTest();
//    publishHotelRedirectionByMongoId.publishHotelRedirectionByMongoIdTest();
  }
}

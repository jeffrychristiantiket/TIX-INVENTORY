package com.tiket.inventory.controller;


import com.tiket.inventory.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@CrossOrigin("http://localhost:8080")
@RequestMapping("/sync")
public class SyncControlleer {

  @Autowired
  SyncService syncService;

  @RequestMapping(path = "/hotel", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity syncHotel(@RequestParam("file") MultipartFile file) {
    syncService.rollbackHotelPublicId(file);
    return ResponseEntity.ok("ok");
  }

}

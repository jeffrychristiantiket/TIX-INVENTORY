package com.tiket.inventory.controller;


import com.tiket.inventory.service.CheckHotelLocationsByLonLat;
import com.tiket.inventory.service.PublishHotel;
import com.tiket.inventory.service.SyncAtlasIdService;
import com.tiket.inventory.service.SyncHotelRawToHotel;
import com.tiket.inventory.service.SyncIsDeleted;
import com.tiket.inventory.service.SyncMissingAtlasIdByLocationMongoId;
import com.tiket.inventory.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/sync")
public class SyncController {

  @Autowired
  SyncService syncService;

  @Autowired
  SyncIsDeleted syncIsDeleted;

  @Autowired
  PublishHotel publishHotel;

  @Autowired
  SyncAtlasIdService syncAtlasIdService;

  @Autowired
  CheckHotelLocationsByLonLat checkHotelLocationsByLonLat;

  @Autowired
  SyncHotelRawToHotel syncHotelRawToHotel;

  @Autowired
  SyncMissingAtlasIdByLocationMongoId syncMissingAtlasIdByLocationMongoId;

  @RequestMapping(path = "/hotel", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity syncHotel(@RequestParam("file") MultipartFile file) {
    syncService.rollbackHotelPublicId(file);
    return ResponseEntity.ok("ok");
  }

  @RequestMapping(path = "/replace-is-deleted", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity replaceDeletedById(@RequestParam("file") MultipartFile file) {
    syncIsDeleted.replaceDeleted(file);
    return ResponseEntity.ok("ok");
  }

  @RequestMapping(path = "/publish-hotel", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity publishHotel(@RequestParam("file") MultipartFile file) {
    publishHotel.publishHotel(file);
    return ResponseEntity.ok("ok");
  }

  @RequestMapping(path = "/sync-atlas-id", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity syncAtlasId(@RequestParam("file") MultipartFile file) {
    syncAtlasIdService.sync(file);
    return ResponseEntity.ok("ok");
  }

  @RequestMapping(path = "/check-hotel-locations-by-lon-lat", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity checkHotelLocationsByLonLat(@RequestParam("file") MultipartFile file) {
    checkHotelLocationsByLonLat.verifyAndExportToCsv(file);
    return ResponseEntity.ok("ok");
  }

  @RequestMapping(path = "/sync-hotel-raw-to-hotel-by-hotel-ids", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity syncHotelRawToHotelByHotelIds(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "vendorExternalIds", required = false) String vendorExternalIds,
      @RequestParam(value = "vendor", required = false) String vendor,
      @RequestParam(value = "unSyncRaw", defaultValue = "false") Boolean unSyncRaw) {
    syncHotelRawToHotel.sync(vendorExternalIds, vendor, unSyncRaw, file);
    return ResponseEntity.ok("ok");
  }

  @RequestMapping(path = "/sync-missing-atlas-id-by-location-mongo-id", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity syncMissingAtlasIdByLocationMongoId(
      @RequestParam("file") MultipartFile file,
      @RequestParam("collectionName") String collectionName) {
    syncMissingAtlasIdByLocationMongoId.sync(file, collectionName);
    return ResponseEntity.ok("ok");
  }

}

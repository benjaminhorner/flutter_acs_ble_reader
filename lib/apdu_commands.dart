class APDUCommands {
  static const apduSelectLength2Bytes = "00 A4 02 0C 02";
  static const apduSelectLength6Bytes = "00 A4 04 0C 06";
  static const mf = "$apduSelectLength2Bytes 3F 00";
  static const efICC = "$apduSelectLength2Bytes 00 02";
  static const efIC = "$apduSelectLength2Bytes 00 05";
  static const dfTachograph = "$apduSelectLength6Bytes FF 54 41 43 48 4F";
  static const efAppIdentification = "$apduSelectLength2Bytes 05 01";
  static const efIdentification = "$apduSelectLength2Bytes 05 20";
  static const efCardDownload = "$apduSelectLength2Bytes 05 0E";
  static const efDrivingLicenceInfo = "$apduSelectLength2Bytes 05 21";
  static const efEventsData = "$apduSelectLength2Bytes 05 02";
  static const efFaultsData = "$apduSelectLength2Bytes 05 03";
  static const efDriverActivityData = "$apduSelectLength2Bytes 05 04";
  static const efVehiclesUsed = "$apduSelectLength2Bytes 05 05";
  static const efPlaces = "$apduSelectLength2Bytes 05 06";
  static const efCurrentUsage = "$apduSelectLength2Bytes 05 07";
  static const efControlActivityData = "$apduSelectLength2Bytes 05 08";
  static const efSpecificConditions = "$apduSelectLength2Bytes 05 22";
  static const efCardCertificate = "$apduSelectLength2Bytes C1 00";
  static const efCaCertificate = "$apduSelectLength2Bytes C1 08";
  static const efTachographG2 = "$apduSelectLength6Bytes FF 53 4D 52 44 54";
  static const efVehicleUnitsUsed = "$apduSelectLength2Bytes 05 23";
  static const efGNSSPlaces = "$apduSelectLength2Bytes 05 24";
  static const efCardSignCertificate = "$apduSelectLength2Bytes C1 01";
  static const efLinkcertificate = "$apduSelectLength2Bytes C1 09";

  static List<String> getCommands() {
    List<String> list = [];
    list.addAll([
      mf,
      efICC,
      efIC,
      dfTachograph,
      efAppIdentification,
      efIdentification,
      efCardDownload,
      efDrivingLicenceInfo,
      efEventsData,
      efFaultsData,
      efDriverActivityData,
      efVehiclesUsed,
      efPlaces,
      efCurrentUsage,
      efControlActivityData,
      efSpecificConditions,
      efCardCertificate,
      efTachographG2,
      efVehicleUnitsUsed,
      efGNSSPlaces,
      efCardSignCertificate,
      efLinkcertificate,
    ]);
    return list;
  }
}

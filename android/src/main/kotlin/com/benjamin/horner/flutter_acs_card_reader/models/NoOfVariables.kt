package com.benjamin.horner.flutter_acs_card_reader

data class NoOfVarModel (
    var noOfEventsPerType: Int = 0,
    var noOfFaultsPerType: Int = 0,
    var noOfCardVehicleRecords: Int = 0,
    var noOfCardPlaceRecords: Int = 0,
    var cardActivityLengthRange: Int = 0,
    var noOfGNSSRecords: Int = 0
)
//
//  NoOfVarModel.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 22/11/2023.
//

import Foundation

struct NoOfVarModel {
    var noOfEventsPerType: Int
        var noOfFaultsPerType: Int
        var noOfCardVehicleRecords: Int
        var noOfCardPlaceRecords: Int
        var cardActivityLengthRange: Int
        var noOfGNSSRecords: Int
        var noOfCardVehicleUnitRecords: Int
        var noOfSpecificConditionsRecords: Int
    
    init(noOfEventsPerType: Int = 0, noOfFaultsPerType: Int = 0, noOfCardVehicleRecords: Int = 0, noOfCardPlaceRecords: Int = 0, cardActivityLengthRange: Int = 0, noOfGNSSRecords: Int = 0, noOfCardVehicleUnitRecords: Int = 0, noOfSpecificConditionsRecords: Int = 0) {
        self.noOfEventsPerType = noOfEventsPerType
        self.noOfFaultsPerType = noOfFaultsPerType
        self.noOfCardVehicleRecords = noOfCardVehicleRecords
        self.noOfCardPlaceRecords = noOfCardPlaceRecords
        self.cardActivityLengthRange = cardActivityLengthRange
        self.noOfGNSSRecords = noOfGNSSRecords
        self.noOfCardVehicleUnitRecords = noOfCardVehicleUnitRecords
        self.noOfSpecificConditionsRecords = noOfSpecificConditionsRecords
    }
}

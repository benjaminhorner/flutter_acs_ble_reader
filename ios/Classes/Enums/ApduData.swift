//
//  ApduData.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 23/11/2023.
//

import Foundation

struct ApduData {
    var name: String
    var data: String
    var offset: Int

    init(name: String = "", data: String = "", offset: Int = 0) {
        self.name = name
        self.data = data
        self.offset = offset
    }
}

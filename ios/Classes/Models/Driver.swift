//
//  Driver.swift
//  flutter_acs_card_reader
//
//  Created by Benjamin Horner on 22/11/2023.
//

import Foundation

struct Driver {
    var card: String
    var name: String
    var firstName: String
    var email: String
    var phone: String
    let agencyID: String

    init(card: String, name: String = "", firstName: String = "", email: String = "", phone: String = "", agencyID: String = "") {
        self.card = card
        self.name = name
        self.firstName = firstName
        self.email = email
        self.phone = phone
        self.agencyID = agencyID
    }
}

extension Driver {
    func toMap() -> [String: Any] {
        return [
            "card": card,
            "name": name,
            "firstName": firstName,
            "email": email,
            "phone": phone,
            "agencyID": agencyID
        ]
    }
}

class User {
  Conducteur? conducteur;
  Agence? agence;
  bool? estConnecte;

  User({this.conducteur, this.agence, this.estConnecte});

  User.fromJson(Map<String, dynamic> json) {
    conducteur = json['conducteur'] != null
        ? Conducteur?.fromJson(json['conducteur'])
        : null;
    agence = json['agence'] != null ? Agence?.fromJson(json['agence']) : null;
    estConnecte = json['estConnecte'];
  }

  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    if (conducteur != null) {
      data['conducteur'] = conducteur?.toJson();
    }
    if (agence != null) {
      data['agence'] = agence?.toJson();
    }
    data['estConnecte'] = estConnecte;
    return data;
  }
}

class Conducteur {
  String? nom;
  String? prenom;
  String? tel;
  String? email;
  String? carte;

  Conducteur({this.nom, this.prenom, this.tel, this.email, this.carte});

  Conducteur.fromJson(Map<String, dynamic> json) {
    nom = json['nom'];
    prenom = json['prenom'];
    tel = json['tel'];
    email = json['email'];
    carte = json['carte'];
  }

  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['nom'] = nom;
    data['prenom'] = prenom;
    data['tel'] = tel;
    data['email'] = email;
    data['carte'] = carte;
    return data;
  }
}

class Agence {
  int? iD;
  String? emails;

  Agence({this.iD, this.emails});

  Agence.fromJson(Map<String, dynamic> json) {
    iD = json['ID'];
    emails = json['emails'];
  }

  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['ID'] = iD;
    data['emails'] = emails;
    return data;
  }
}

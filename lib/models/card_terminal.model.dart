class CardTerminal {
  String? name;
  bool? isCardPresent;

  CardTerminal({
    this.name,
    this.isCardPresent,
  });

  CardTerminal.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    isCardPresent = json['isCardPresent'];
  }

  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['name'] = name;
    data['isCardPresent'] = isCardPresent;
    return data;
  }
}

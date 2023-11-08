class ResponseData {
  String? interim, agencyID, fileData, countryCode;

  ResponseData({
    this.interim,
    this.agencyID,
    this.fileData,
    this.countryCode,
  });

  ResponseData.fromJson(Map<String, dynamic> json) {
    interim = json['interim'];
    agencyID = json['agencyID'];
    fileData = json['fileData'];
    countryCode = json['countryCode'];
  }

  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['interim'] = interim;
    data['agencyID'] = agencyID;
    data['fileData'] = fileData;
    data['countryCode'] = countryCode;
    return data;
  }
}

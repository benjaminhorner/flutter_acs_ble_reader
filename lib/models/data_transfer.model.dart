class ResponseData {
  String? interim, agencyID, fileData;

  ResponseData({
    this.interim,
    this.agencyID,
    this.fileData,
  });

  ResponseData.fromJson(Map<String, dynamic> json) {
    interim = json['interim'];
    agencyID = json['agencyID'];
    fileData = json['fileData'];
  }

  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['interim'] = interim;
    data['agencyID'] = agencyID;
    data['fileData'] = fileData;
    return data;
  }
}

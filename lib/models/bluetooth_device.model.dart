class BluetoothDevice {
  final String name;
  final String address;
  final int type;
  final int bondState;

  BluetoothDevice({
    required this.name,
    required this.address,
    required this.type,
    required this.bondState,
  });

  factory BluetoothDevice.fromMap(Map<String, dynamic> map) {
    return BluetoothDevice(
      name: map['name'] ?? '',
      address: map['address'] ?? '',
      type: map['type'] ?? 0,
      bondState: map['bondState'] ?? 0,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'name': name,
      'address': address,
      'type': type,
      'bondState': bondState,
    };
  }
}

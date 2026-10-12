import 'package:package_info_plus/package_info_plus.dart';

const String appName = 'Disk Partition Calculator';

Future<String> getAppVersion() async {
  final info = await PackageInfo.fromPlatform();
  return info.version;
}

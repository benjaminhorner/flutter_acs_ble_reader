#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_acs_card_reader.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_acs_card_reader'
  s.version          = '0.7.2'
  s.summary          = 'ACS Card Reader Plugin.'
  s.description      = <<-DESC
ACS Card Reader Plugin.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Benjamin Horner' => 'b.e.horner@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'CryptoSwift'
  s.platform = :ios, '11.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  # Vendor Frameworks
  s.preserve_paths = 'Frameworks/ACSSmartCardIO.xcframework/**/*', 'Frameworks/SmartCardIO.xcframework/**/*'
  s.xcconfig = { 'OTHER_LDFLAGS' => '-framework ACSSmartCardIO' }
  s.vendored_frameworks = ['Frameworks/ACSSmartCardIO.xcframework', 'Frameworks/SmartCardIO.xcframework']
end

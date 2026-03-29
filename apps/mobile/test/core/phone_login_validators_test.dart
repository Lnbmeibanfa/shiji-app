import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/features/auth/validators/phone_login_validators.dart';

void main() {
  group('PhoneLoginValidators', () {
    test('phone accepts 1 + 10 digits', () {
      expect(PhoneLoginValidators.isValidPhone('13800138000'), isTrue);
      expect(PhoneLoginValidators.isValidPhone('02012345678'), isFalse);
      expect(PhoneLoginValidators.isValidPhone('23800138000'), isFalse);
      expect(PhoneLoginValidators.isValidPhone('1380013800'), isFalse);
    });

    test('sms code 4–8 digits', () {
      expect(PhoneLoginValidators.isValidSmsCode('1234'), isTrue);
      expect(PhoneLoginValidators.isValidSmsCode('12345678'), isTrue);
      expect(PhoneLoginValidators.isValidSmsCode('123'), isFalse);
      expect(PhoneLoginValidators.isValidSmsCode('123456789'), isFalse);
    });
  });
}

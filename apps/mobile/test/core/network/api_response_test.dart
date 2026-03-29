import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exceptions.dart';
import 'package:mobile/core/network/api_response.dart';

void main() {
  group('ApiEnvelope', () {
    test('code 为 0 时解析 data', () {
      final v = ApiEnvelope.parse<int>(
        <String, dynamic>{'code': 0, 'message': 'success', 'data': 42},
        (raw) => raw! as int,
      );
      expect(v, 42);
    });

    test('code 非 0 时抛出 ApiBusinessException', () {
      expect(
        () => ApiEnvelope.parse<int>(
          <String, dynamic>{
            'code': 10001,
            'message': '验证码发送过于频繁',
            'data': null,
          },
          (_) => 0,
        ),
        throwsA(isA<ApiBusinessException>()),
      );
    });
  });
}

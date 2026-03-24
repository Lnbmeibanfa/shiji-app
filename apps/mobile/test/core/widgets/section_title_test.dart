import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_typography.dart';
import 'package:mobile/core/widgets/section_title.dart';

void main() {
  group('SectionTitle', () {
    testWidgets('标题使用 titleSmall 样式', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: SectionTitle(title: '今日记录'),
          ),
        ),
      );

      final text = tester.widget<Text>(find.text('今日记录'));
      expect(text.style?.fontSize, AppTypography.titleSmall().fontSize);
      expect(text.style?.fontWeight, AppTypography.titleSmall().fontWeight);
    });
  });
}

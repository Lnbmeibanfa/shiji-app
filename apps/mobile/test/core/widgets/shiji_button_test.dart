import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_sizes.dart';
import 'package:mobile/core/widgets/shiji_button.dart';

void main() {
  group('ShijiButton', () {
    testWidgets('启用态：主色背景、高度 52', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ShijiButton(label: '保存', onPressed: _noop),
          ),
        ),
      );

      final sizedBox = tester.widget<SizedBox>(
        find.descendant(
          of: find.byType(ShijiButton),
          matching: find.byType(SizedBox),
        ).first,
      );
      expect(sizedBox.height, AppSizes.buttonPrimaryHeight);

      final material = tester.widget<Material>(
        find.descendant(
          of: find.byType(ShijiButton),
          matching: find.byType(Material),
        ).first,
      );
      expect(material.color, AppColors.primary);
      expect(find.text('保存'), findsOneWidget);
    });

    testWidgets('禁用态：禁用背景色', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ShijiButton(label: '保存'),
          ),
        ),
      );

      final material = tester.widget<Material>(
        find.descendant(
          of: find.byType(ShijiButton),
          matching: find.byType(Material),
        ).first,
      );
      expect(material.color, AppColors.buttonDisabledBackground);
    });
  });
}

void _noop() {}

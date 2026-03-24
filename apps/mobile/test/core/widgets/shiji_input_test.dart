import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_sizes.dart';
import 'package:mobile/core/widgets/shiji_input.dart';

void main() {
  group('ShijiInput', () {
    testWidgets('高度 48、占位符为 tertiary 色', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ShijiInput(hintText: '请输入'),
          ),
        ),
      );

      final sizedBox = tester.widget<SizedBox>(
        find.descendant(
          of: find.byType(ShijiInput),
          matching: find.byType(SizedBox),
        ).first,
      );
      expect(sizedBox.height, AppSizes.inputSingleLineHeight);

      final decorator = tester.widget<InputDecorator>(
        find.descendant(
          of: find.byType(TextField),
          matching: find.byType(InputDecorator),
        ).first,
      );
      expect(decorator.decoration.hintStyle?.color, AppColors.textTertiary);
      expect(decorator.decoration.fillColor, AppColors.bgSecondary);
    });
  });
}

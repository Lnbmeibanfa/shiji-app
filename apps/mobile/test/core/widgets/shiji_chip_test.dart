import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_radius.dart';
import 'package:mobile/core/widgets/shiji_chip.dart';

void main() {
  group('ShijiChip', () {
    testWidgets('默认态：中性标签色、胶囊圆角', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ShijiChip(label: '早餐'),
          ),
        ),
      );

      final container = tester.widget<Container>(
        find.descendant(
          of: find.byType(ShijiChip),
          matching: find.byType(Container),
        ).first,
      );
      final decoration = container.decoration! as BoxDecoration;
      expect(decoration.color, AppColors.tagNeutralBg);
      expect(decoration.borderRadius, BorderRadius.circular(AppRadius.pill));
    });

    testWidgets('选中态：主色背景', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ShijiChip(label: '早餐', selected: true),
          ),
        ),
      );

      final container = tester.widget<Container>(
        find.descendant(
          of: find.byType(ShijiChip),
          matching: find.byType(Container),
        ).first,
      );
      final decoration = container.decoration! as BoxDecoration;
      expect(decoration.color, AppColors.primary);
    });
  });
}

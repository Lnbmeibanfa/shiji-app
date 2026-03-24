import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_radius.dart';
import 'package:mobile/core/theme/app_spacing.dart';
import 'package:mobile/core/widgets/shiji_card.dart';

void main() {
  group('ShijiCard', () {
    testWidgets('圆角 lg、背景 bgCard、内边距 20', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ShijiCard(child: Text('内容')),
          ),
        ),
      );

      final container = tester.widget<Container>(
        find.descendant(
          of: find.byType(ShijiCard),
          matching: find.byType(Container),
        ).first,
      );
      final decoration = container.decoration! as BoxDecoration;
      expect(decoration.color, AppColors.bgCard);
      expect(decoration.borderRadius, BorderRadius.circular(AppRadius.lg));
      expect(container.padding, AppSpacing.cardPadding);
    });
  });
}

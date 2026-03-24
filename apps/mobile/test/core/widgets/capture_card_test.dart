import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_radius.dart';
import 'package:mobile/core/theme/app_sizes.dart';
import 'package:mobile/core/widgets/capture_card.dart';

void main() {
  group('CaptureCard', () {
    testWidgets('高度 172、主色底、圆角 xl', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: CaptureCard(title: '记录饮食'),
          ),
        ),
      );

      final ink = tester.widget<Ink>(
        find.descendant(
          of: find.byType(CaptureCard),
          matching: find.byType(Ink),
        ).first,
      );
      expect(ink.height, AppSizes.captureCardHeight);

      final decoration = ink.decoration as BoxDecoration;
      expect(decoration.color, AppColors.primary);
      expect(decoration.borderRadius, BorderRadius.circular(AppRadius.xl));
    });
  });
}

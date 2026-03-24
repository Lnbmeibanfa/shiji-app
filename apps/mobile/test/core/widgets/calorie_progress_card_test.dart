import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_typography.dart';
import 'package:mobile/core/widgets/calorie_progress_card.dart';

void main() {
  group('CalorieProgressCard', () {
    testWidgets('数字样式为 numberLarge，进度条轨道与前景色符合规范', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: CalorieProgressCard(kcalLabel: '1200 kcal', progress: 0.5),
          ),
        ),
      );

      final kcalText = tester.widget<Text>(find.text('1200 kcal'));
      expect(kcalText.style?.fontSize, AppTypography.numberLarge().fontSize);
      expect(kcalText.style?.fontWeight, AppTypography.numberLarge().fontWeight);

      final progress = tester.widget<LinearProgressIndicator>(
        find.byType(LinearProgressIndicator),
      );
      expect(progress.backgroundColor, AppColors.progressTrack);
      expect(progress.color, AppColors.primary);
    });
  });
}

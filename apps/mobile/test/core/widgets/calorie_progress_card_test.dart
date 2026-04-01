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

    testWidgets('摘要模式：未超目标时进度条为主色', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: CalorieProgressCard.summary(
              consumedKcal: 800,
              remainingKcal: 800,
              goalKcal: 1600,
            ),
          ),
        ),
      );

      final progress = tester.widget<LinearProgressIndicator>(
        find.byType(LinearProgressIndicator),
      );
      expect(progress.value, 0.5);
      expect(progress.color, AppColors.primary);
    });

    testWidgets('摘要模式：达到目标时满条且为 progressOverBudget 色', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: CalorieProgressCard.summary(
              consumedKcal: 1600,
              remainingKcal: 0,
              goalKcal: 1600,
            ),
          ),
        ),
      );

      final progress = tester.widget<LinearProgressIndicator>(
        find.byType(LinearProgressIndicator),
      );
      expect(progress.value, 1.0);
      expect(progress.color, AppColors.progressOverBudget);
    });

    testWidgets('摘要模式：超过目标时仍满条且为红色', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: CalorieProgressCard.summary(
              consumedKcal: 1800,
              remainingKcal: 0,
              goalKcal: 1600,
            ),
          ),
        ),
      );

      final progress = tester.widget<LinearProgressIndicator>(
        find.byType(LinearProgressIndicator),
      );
      expect(progress.value, 1.0);
      expect(progress.color, AppColors.progressOverBudget);
    });
  });
}

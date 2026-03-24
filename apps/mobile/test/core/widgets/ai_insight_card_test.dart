import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/core/theme/app_radius.dart';
import 'package:mobile/core/widgets/ai_insight_card.dart';

void main() {
  group('AIInsightCard', () {
    testWidgets('外层 accentWarm、icon 区背景、内层 accentWarmInner', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: AIInsightCard(
              title: '今日小结',
              body: '整体均衡',
              suggestion: '可以多摄入蔬菜',
            ),
          ),
        ),
      );

      final outer = tester.widget<Container>(
        find.descendant(
          of: find.byType(AIInsightCard),
          matching: find.byType(Container),
        ).first,
      );
      final outerDeco = outer.decoration! as BoxDecoration;
      expect(outerDeco.color, AppColors.accentWarm);
      expect(outerDeco.borderRadius, BorderRadius.circular(AppRadius.lg));

      final iconAreaFinder = find.descendant(
        of: find.byType(AIInsightCard),
        matching: find.byWidgetPredicate(
          (w) =>
              w is Container &&
              w.decoration is BoxDecoration &&
              (w.decoration! as BoxDecoration).color ==
                  AppColors.aiInsightIconBackground,
        ),
      );
      expect(iconAreaFinder, findsOneWidget);
      final iconArea = tester.widget<Container>(iconAreaFinder);
      final iconDeco = iconArea.decoration! as BoxDecoration;
      expect(iconDeco.color, AppColors.aiInsightIconBackground);

      final innerFinder = find.descendant(
        of: find.byType(AIInsightCard),
        matching: find.byWidgetPredicate(
          (w) =>
              w is Container &&
              w.decoration is BoxDecoration &&
              (w.decoration! as BoxDecoration).color ==
                  AppColors.accentWarmInner,
        ),
      );
      expect(innerFinder, findsOneWidget);
      final innerContainer = tester.widget<Container>(innerFinder);
      final innerDeco = innerContainer.decoration! as BoxDecoration;
      expect(innerDeco.color, AppColors.accentWarmInner);
      expect(innerDeco.borderRadius, BorderRadius.circular(AppRadius.md));
    });
  });
}

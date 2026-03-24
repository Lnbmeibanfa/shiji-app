import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_radius.dart';
import 'package:mobile/core/theme/app_sizes.dart';
import 'package:mobile/core/widgets/meal_record_card.dart';

void main() {
  group('MealRecordCard', () {
    testWidgets('compact：图片区域 76×76、图片圆角 md', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: MealRecordCard(
              title: '午餐',
              subtitle: '12:30',
              variant: MealRecordCardVariant.compact,
              image: Container(color: Colors.grey),
            ),
          ),
        ),
      );

      final sizedBox = tester.widget<SizedBox>(
        find.descendant(
          of: find.byType(ClipRRect),
          matching: find.byType(SizedBox),
        ).first,
      );
      expect(sizedBox.width, AppSizes.mealImageCompact);
      expect(sizedBox.height, AppSizes.mealImageCompact);

      final clip = tester.widget<ClipRRect>(find.byType(ClipRRect).first);
      expect(clip.borderRadius, BorderRadius.circular(AppRadius.md));
    });

    testWidgets('full：图片区域 96×96', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: MealRecordCard(
              title: '午餐',
              subtitle: '12:30',
              variant: MealRecordCardVariant.full,
              image: Container(color: Colors.grey),
            ),
          ),
        ),
      );

      final sizedBox = tester.widget<SizedBox>(
        find.descendant(
          of: find.byType(ClipRRect),
          matching: find.byType(SizedBox),
        ).first,
      );
      expect(sizedBox.width, AppSizes.mealImageFull);
      expect(sizedBox.height, AppSizes.mealImageFull);
    });
  });
}

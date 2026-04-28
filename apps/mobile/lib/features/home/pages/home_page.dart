import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/models/latest_meal_record_response.dart';
import '../../../core/providers.dart';
import '../../../core/routing/route_paths.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/ai_insight_card.dart';
import '../../../core/widgets/calorie_progress_card.dart';
import '../../../core/widgets/capture_card.dart';
import '../../../core/widgets/meal_record_card.dart';
import '../../../core/widgets/section_title.dart';

/// 首页：问候、拍照入口、热量摘要、最近记录、AI 提示（数据 mock，拍照留扩展点）。
class HomePage extends ConsumerWidget {
  const HomePage({super.key, this.now});

  /// 注入固定时间便于测试；为 null 时使用 [DateTime.now]。
  final DateTime? now;

  static const _slogan = '今天也一起轻松吃饭吧';

  static const _mockConsumed = 1245;
  static const _mockRemaining = 355;
  static const _mockGoal = 1600;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final d = now ?? DateTime.now();
    final latestMeal = ref.watch(_latestMealProvider);

    return ColoredBox(
      color: AppColors.bgPrimary,
      child: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(
            AppSpacing.s24,
            AppSpacing.s16,
            AppSpacing.s24,
            AppSpacing.s24,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _HomeGreeting(dateLine: _formatDateLine(d), slogan: _slogan),
              const SizedBox(height: AppSpacing.s20),
              CaptureCard(
                title: '拍一顿',
                onTap: () => context.push(RoutePaths.recordMeal),
              ),
              const SizedBox(height: AppSpacing.s20),
              CalorieProgressCard.summary(
                consumedKcal: _mockConsumed,
                remainingKcal: _mockRemaining,
                goalKcal: _mockGoal,
              ),
              const SizedBox(height: AppSpacing.s24),
              SectionTitle(
                title: '最近记录',
                trailing: GestureDetector(
                  onTap: () => _openRecordTab(context),
                  child: Text(
                    '查看全部',
                    style: AppTypography.bodySmall(
                      color: AppColors.textTertiary,
                    ),
                  ),
                ),
              ),
              latestMeal.when(
                loading: _buildLatestMealLoading,
                error: (error, stackTrace) => _buildLatestMealError(),
                data: (data) => data == null
                    ? _buildLatestMealEmpty(context)
                    : _buildLatestMealCard(data),
              ),
              const SizedBox(height: AppSpacing.s24),
              AIInsightCard(
                title: '今日小结',
                body:
                    '今天整体控制得不错，晚餐稍微清淡一点会更舒服',
                icon: Icons.auto_awesome_outlined,
              ),
            ],
          ),
        ),
      ),
    );
  }

  static MealRecordCard _buildLatestMealLoading() {
    return _baseCard(title: '最近一餐加载中', subtitle: '请稍候...');
  }

  static MealRecordCard _buildLatestMealError() {
    return _baseCard(title: '最近一餐', subtitle: '加载失败，稍后再试');
  }

  static Widget _buildLatestMealEmpty(BuildContext context) {
    return GestureDetector(
      onTap: () => context.push(RoutePaths.recordMeal),
      child: _baseCard(title: '还没有记录', subtitle: '去记录一餐'),
    );
  }

  static MealRecordCard _buildLatestMealCard(LatestMealRecordResponse data) {
    final mealType = _mealTypeLabel(data.mealType);
    final time = _formatHm(data.recordedAt);
    final kcal = data.totalEstimatedCalories?.round();
    final kcalText = kcal == null ? '-- kcal' : '$kcal kcal';
    return _baseCard(title: '$mealType $time', subtitle: kcalText);
  }

  static MealRecordCard _baseCard({
    required String title,
    required String subtitle,
  }) {
    return MealRecordCard(
      title: title,
      subtitle: subtitle,
      image: ColoredBox(
        color: AppColors.primarySoft,
        child: Center(
          child: Icon(
            Icons.restaurant_outlined,
            color: AppColors.primary.withValues(alpha: 0.6),
            size: 40,
          ),
        ),
      ),
    );
  }

  static String _formatDateLine(DateTime d) {
    const weekdays = [
      '星期一',
      '星期二',
      '星期三',
      '星期四',
      '星期五',
      '星期六',
      '星期日',
    ];
    return '${d.year}年${d.month}月${d.day}日，${weekdays[d.weekday - 1]}';
  }

  static void _openRecordTab(BuildContext context) {
    final shell = StatefulNavigationShell.maybeOf(context);
    shell?.goBranch(1);
  }

  static String _formatHm(DateTime t) {
    final hh = t.hour.toString().padLeft(2, '0');
    final mm = t.minute.toString().padLeft(2, '0');
    return '$hh:$mm';
  }

  static String _mealTypeLabel(String mealType) {
    return switch (mealType) {
      'breakfast' => '早餐',
      'lunch' => '午餐',
      'dinner' => '晚餐',
      'snack' => '加餐',
      'extra_meal' => '额外一餐',
      _ => '一餐',
    };
  }
}

final _latestMealProvider = FutureProvider<LatestMealRecordResponse?>((ref) {
  return ref.watch(homeRepositoryProvider).getLatestMeal();
});

class _HomeGreeting extends StatelessWidget {
  const _HomeGreeting({
    required this.dateLine,
    required this.slogan,
  });

  final String dateLine;
  final String slogan;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          dateLine,
          style: AppTypography.bodySmall(color: AppColors.textTertiary),
        ),
        const SizedBox(height: AppSpacing.s8),
        Text(
          slogan,
          style: AppTypography.titleLarge(),
        ),
      ],
    );
  }
}

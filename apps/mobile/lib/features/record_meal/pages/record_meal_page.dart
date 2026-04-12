import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/feedback/app_feedback.dart';
import '../../../core/network/api_exceptions.dart';
import '../../../core/network/models/file_upload_response.dart';
import '../../../core/providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import '../models/draft_food_item.dart';
import '../widgets/emotion_chips_section.dart';
import '../widgets/manual_add_food_button.dart';
import '../widgets/meal_notes_field.dart';
import '../widgets/meal_type_selector.dart';
import '../widgets/recent_meals_placeholder.dart';
import '../widgets/record_meal_photo_section.dart';
import '../widgets/record_meal_save_bar.dart';

/// 记录饮食：照片、餐别、最近餐占位、手动食物、情绪、备注、保存。
class RecordMealPage extends ConsumerStatefulWidget {
  const RecordMealPage({super.key});

  @override
  ConsumerState<RecordMealPage> createState() => _RecordMealPageState();
}

class _RecordMealPageState extends ConsumerState<RecordMealPage> {
  String _mealType = MealTypes.lunch;
  FileUploadResponse? _uploaded;
  final List<DraftFoodItem> _foods = [];
  final TextEditingController _noteController = TextEditingController();
  Set<int> _emotionIds = {};
  bool _saving = false;

  @override
  void dispose() {
    _noteController.dispose();
    super.dispose();
  }

  String get _recordMethod => _uploaded == null ? 'manual' : 'photo';

  bool get _canSave => _foods.isNotEmpty;

  Future<void> _save() async {
    if (!_canSave || _saving) return;

    setState(() => _saving = true);
    try {
      final body = _buildRequestBody();
      await ref.read(mealRecordRepositoryProvider).createMealRecord(body);
      if (!mounted) return;
      AppFeedback.showToast(
        context,
        kind: FeedbackToastKind.success,
        title: '保存成功',
      );
      context.pop();
    } on ApiBusinessException catch (e) {
      if (mounted) {
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: e.message.isNotEmpty ? e.message : '保存失败（${e.code}）',
        );
      }
    } on ApiHttpException catch (e) {
      if (mounted) {
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: e.message ?? '网络异常',
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Map<String, dynamic> _buildRequestBody() {
    final note = _noteController.text.trim();
    return <String, dynamic>{
      'mealType': _mealType,
      'recordedAt': DateTime.now().toIso8601String(),
      if (note.isNotEmpty) 'note': note,
      'recordMethod': _recordMethod,
      'completionStatus': 'completed',
      'recognitionStatus': 'skipped',
      'foodItems': [
        for (var i = 0; i < _foods.length; i++)
          <String, dynamic>{
            'foodNameSnapshot': _foods[i].name,
            'recognitionSource': 'user_manual',
            'displayUnit': 'g',
            'sortOrder': i,
            if (_foods[i].estimatedCalories != null)
              'estimatedCalories': _foods[i].estimatedCalories,
          },
      ],
      'images': _uploaded == null
          ? <Map<String, dynamic>>[]
          : [
              <String, dynamic>{
                'fileId': _uploaded!.fileId,
                'isPrimary': 1,
                'sortOrder': 0,
              },
            ],
      'emotions': <Map<String, dynamic>>[],
    };
  }

  @override
  Widget build(BuildContext context) {
    return ColoredBox(
      color: AppColors.bgPrimary,
      child: Scaffold(
        backgroundColor: AppColors.bgPrimary,
        appBar: AppBar(
          backgroundColor: AppColors.bgPrimary,
          elevation: 0,
          leading: IconButton(
            icon: const Icon(Icons.arrow_back_ios_new_rounded),
            color: AppColors.textPrimary,
            onPressed: () => context.pop(),
          ),
          title: Text(
            '记录饮食',
            style: AppTypography.titleMedium(color: AppColors.textPrimary),
          ),
          centerTitle: true,
        ),
        body: Column(
          children: [
            Expanded(
              child: SafeArea(
                bottom: false,
                child: SingleChildScrollView(
                  padding: const EdgeInsets.symmetric(horizontal: AppSpacing.s24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      RecordMealPhotoSection(
                        onUploadedChanged: (v) {
                          setState(() => _uploaded = v);
                        },
                      ),
                      const SizedBox(height: AppSpacing.s24),
                      MealTypeSelector(
                        value: _mealType,
                        onChanged: (v) => setState(() => _mealType = v),
                      ),
                      const SizedBox(height: AppSpacing.s24),
                      RecentMealsPlaceholder(mealType: _mealType),
                      const SizedBox(height: AppSpacing.s16),
                      ManualAddFoodButton(
                        onAdd: (item) {
                          setState(() => _foods.add(item));
                        },
                      ),
                      if (_foods.isNotEmpty) ...[
                        const SizedBox(height: AppSpacing.s16),
                        _FoodItemsList(
                          items: _foods,
                          onRemoveAt: (i) {
                            setState(() => _foods.removeAt(i));
                          },
                        ),
                      ],
                      const SizedBox(height: AppSpacing.s24),
                      EmotionChipsSection(
                        selectedIds: _emotionIds,
                        onChanged: (s) => setState(() => _emotionIds = s),
                      ),
                      const SizedBox(height: AppSpacing.s24),
                      MealNotesField(controller: _noteController),
                      const SizedBox(height: AppSpacing.s24),
                    ],
                  ),
                ),
              ),
            ),
            RecordMealSaveBar(
              canSave: _canSave,
              loading: _saving,
              onSave: _save,
            ),
          ],
        ),
      ),
    );
  }
}

class _FoodItemsList extends StatelessWidget {
  const _FoodItemsList({
    required this.items,
    required this.onRemoveAt,
  });

  final List<DraftFoodItem> items;
  final ValueChanged<int> onRemoveAt;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '已添加食物',
          style: AppTypography.titleSmall(color: AppColors.textPrimary),
        ),
        const SizedBox(height: AppSpacing.s8),
        for (var i = 0; i < items.length; i++)
          Padding(
            padding: const EdgeInsets.only(bottom: AppSpacing.s8),
            child: Material(
              color: AppColors.bgMuted,
              borderRadius: BorderRadius.circular(AppRadius.md),
              child: ListTile(
                title: Text(
                  items[i].name,
                  style: AppTypography.bodyLarge(color: AppColors.textPrimary),
                ),
                subtitle: items[i].estimatedCalories != null
                    ? Text(
                        '${items[i].estimatedCalories!.toStringAsFixed(0)} kcal',
                        style:
                            AppTypography.bodySmall(color: AppColors.primary),
                      )
                    : null,
                trailing: IconButton(
                  icon: const Icon(Icons.close_rounded),
                  color: AppColors.textTertiary,
                  onPressed: () => onRemoveAt(i),
                ),
              ),
            ),
          ),
      ],
    );
  }
}

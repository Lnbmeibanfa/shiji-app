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
import '../ai_stub_foods.dart';
import '../meal_type_time.dart';
import '../models/draft_food_item.dart';
import '../widgets/emotion_chips_section.dart';
import '../widgets/manual_add_food_button.dart';
import '../widgets/meal_notes_field.dart';
import '../widgets/meal_type_selector.dart';
import '../widgets/recent_meals_placeholder.dart';
import '../widgets/record_meal_photo_section.dart';
import '../widgets/record_meal_save_bar.dart';

/// 记录饮食：照片、餐别、食物列表、情绪、备注、保存。
class RecordMealPage extends ConsumerStatefulWidget {
  const RecordMealPage({super.key});

  @override
  ConsumerState<RecordMealPage> createState() => _RecordMealPageState();
}

class _RecordMealPageState extends ConsumerState<RecordMealPage> {
  late String _mealType;
  FileUploadResponse? _uploaded;
  bool _uploading = false;
  bool _aiRecognizing = false;
  bool _photoAiCompleted = false;
  int _aiSession = 0;

  final List<DraftFoodItem> _foods = [];
  final TextEditingController _noteController = TextEditingController();
  Set<int> _emotionIds = {};
  bool _saving = false;

  /// 后端已支持餐级菜品命中字段；接入真实 AI 时可赋值后随保存请求一并提交。
  int? _matchedDishId;
  String? _matchedDishName;
  String? _dishMatchSource;
  double? _dishMatchConfidence;

  @override
  void initState() {
    super.initState();
    _mealType = mealTypeForNow();
    _emotionIds = {EmotionChipsSection.options.first.id};
  }

  @override
  void dispose() {
    _noteController.dispose();
    super.dispose();
  }

  String get _recordMethod {
    if (_uploaded == null) return 'manual';
    if (_photoAiCompleted) return 'photo_ai';
    return 'photo';
  }

  bool get _canSave =>
      _foods.isNotEmpty && !_uploading && !_aiRecognizing && !_saving;

  void _onUploadedChanged(FileUploadResponse? v) {
    setState(() {
      _uploaded = v;
      if (v == null) {
        _photoAiCompleted = false;
        _foods.removeWhere((e) => e.fromAi);
        _aiSession++;
        _aiRecognizing = false;
      }
    });
    if (v != null) {
      _startAiStub();
    }
  }

  void _startAiStub() {
    final session = ++_aiSession;
    setState(() => _aiRecognizing = true);
    Future<void>.delayed(const Duration(milliseconds: 1500), () {
      if (!mounted) return;
      if (session != _aiSession) return;
      setState(() {
        for (final r in AiStubFoods.rows) {
          _foods.add(
            DraftFoodItem.aiStub(
              foodItemId: r.id,
              name: r.name,
              kcalPer100g: r.kcalPer100g.toDouble(),
            ),
          );
        }
        _photoAiCompleted = true;
        _aiRecognizing = false;
      });
    });
  }

  void _interruptAi() {
    _aiSession++;
    setState(() => _aiRecognizing = false);
  }

  Future<void> _save() async {
    if (!_canSave) return;

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
      if (_matchedDishId != null) 'dishId': _matchedDishId,
      if (_matchedDishName != null) 'dishNameSnapshot': _matchedDishName,
      if (_dishMatchSource != null) 'dishMatchSource': _dishMatchSource,
      if (_dishMatchConfidence != null) 'dishMatchConfidence': _dishMatchConfidence,
      'foodItems': [
        for (var i = 0; i < _foods.length; i++)
          <String, dynamic>{
            'foodNameSnapshot': _foods[i].name,
            'recognitionSource': _foods[i].recognitionSource,
            'displayUnit': 'g',
            'sortOrder': i,
            'estimatedWeightG': _foods[i].weightG,
            if (_foods[i].foodItemId != null) 'foodItemId': _foods[i].foodItemId,
            if (_foods[i].estimatedCalories != null)
              'estimatedCalories': _foods[i].estimatedCalories,
            if (_foods[i].foodItemId != null) 'nutritionCalcBasis': 'food_db',
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

  void _setWeight(int index, double w) {
    setState(() {
      _foods[index] = _foods[index].copyWith(weightG: w);
    });
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
        body: Stack(
          children: [
            Column(
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
                            onUploadedChanged: _onUploadedChanged,
                            onUploadingChanged: (v) => setState(() => _uploading = v),
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
                              onWeightChanged: _setWeight,
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
            if (_aiRecognizing)
              Positioned.fill(
                child: ColoredBox(
                  color: const Color(0x99000000),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const CircularProgressIndicator(color: AppColors.primary),
                      const SizedBox(height: AppSpacing.s24),
                      Text(
                        'AI 识别中…',
                        style: AppTypography.titleSmall(color: AppColors.textInverse),
                      ),
                      const SizedBox(height: AppSpacing.s24),
                      TextButton(
                        onPressed: _interruptAi,
                        child: Text(
                          '手动打断，自行添加食物',
                          style: AppTypography.buttonText(color: AppColors.textInverse),
                        ),
                      ),
                    ],
                  ),
                ),
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
    required this.onWeightChanged,
  });

  final List<DraftFoodItem> items;
  final ValueChanged<int> onRemoveAt;
  final void Function(int index, double weightG) onWeightChanged;

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
              child: Padding(
                padding: const EdgeInsets.all(AppSpacing.s12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Flexible(
                                    child: Text(
                                      items[i].name,
                                      style: AppTypography.bodyLarge(
                                        color: AppColors.textPrimary,
                                      ),
                                    ),
                                  ),
                                  if (items[i].fromAi) ...[
                                    const SizedBox(width: AppSpacing.s8),
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: AppSpacing.s8,
                                        vertical: AppSpacing.s4,
                                      ),
                                      decoration: BoxDecoration(
                                        color: AppColors.primarySoft,
                                        borderRadius:
                                            BorderRadius.circular(AppRadius.sm),
                                      ),
                                      child: Text(
                                        'AI 识别',
                                        style: AppTypography.labelMedium(
                                          color: AppColors.primary,
                                        ).copyWith(fontSize: 11),
                                      ),
                                    ),
                                  ],
                                ],
                              ),
                              if (items[i].kcalPer100g != null) ...[
                                const SizedBox(height: AppSpacing.s4),
                                Text(
                                  '${items[i].kcalPer100g!.toStringAsFixed(0)} kcal/100g',
                                  style: AppTypography.bodySmall(
                                    color: AppColors.textTertiary,
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                        if (items[i].estimatedCalories != null)
                          Text(
                            '${items[i].estimatedCalories!.toStringAsFixed(0)} kcal',
                            style: AppTypography.titleSmall(color: AppColors.primary),
                          ),
                        IconButton(
                          icon: const Icon(Icons.close_rounded),
                          color: AppColors.textTertiary,
                          onPressed: () => onRemoveAt(i),
                        ),
                      ],
                    ),
                    const SizedBox(height: AppSpacing.s8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        IconButton(
                          onPressed: () {
                            final w = (items[i].weightG - 10).clamp(10, 5000);
                            onWeightChanged(i, w.toDouble());
                          },
                          icon: const Icon(Icons.remove_rounded),
                          color: AppColors.textSecondary,
                        ),
                        Text(
                          '${items[i].weightG.toStringAsFixed(0)}g',
                          style: AppTypography.labelMedium(color: AppColors.textPrimary),
                        ),
                        IconButton(
                          onPressed: () {
                            final w = (items[i].weightG + 10).clamp(10, 5000);
                            onWeightChanged(i, w.toDouble());
                          },
                          icon: const Icon(Icons.add_rounded),
                          color: AppColors.textSecondary,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

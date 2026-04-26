import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/feedback/app_feedback.dart';
import '../../../core/network/api_exceptions.dart';
import '../../../core/network/models/file_upload_response.dart';
import '../../../core/network/models/meal_photo_recognition_models.dart';
import '../../../core/providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
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
  bool _recognitionPollCancelled = false;
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

  bool get _canSave => _foods.isNotEmpty && !_uploading && !_saving;

  void _onUploadedChanged(FileUploadResponse? v) {
    setState(() {
      _uploaded = v;
      if (v == null) {
        _photoAiCompleted = false;
        _foods.removeWhere((e) => e.fromAi);
        _aiSession++;
        _aiRecognizing = false;
        _recognitionPollCancelled = true;
      }
    });
    if (v != null) {
      unawaited(_startMealPhotoRecognition(v.fileId));
    }
  }

  Future<void> _startMealPhotoRecognition(int fileId) async {
    final session = ++_aiSession;
    if (!mounted) return;
    setState(() {
      _aiRecognizing = true;
      _recognitionPollCancelled = false;
      _photoAiCompleted = false;
    });
    final repo = ref.read(mealPhotoRecognitionRepositoryProvider);
    final deadline = DateTime.now().add(const Duration(seconds: 60));
    try {
      final created = await repo.createTask(fileId);
      while (mounted && session == _aiSession && !_recognitionPollCancelled) {
        if (DateTime.now().isAfter(deadline)) {
          if (mounted) {
            AppFeedback.showToast(
              context,
              kind: FeedbackToastKind.failure,
              title: '识别超时，可稍后重试或更换图片后重新上传',
            );
          }
          break;
        }
        await Future<void>.delayed(const Duration(seconds: 1));
        if (!mounted || session != _aiSession || _recognitionPollCancelled) {
          break;
        }
        final poll = await repo.poll(created.taskId);
        if (!mounted || session != _aiSession || _recognitionPollCancelled) {
          break;
        }
        if (poll.status == 'success' && poll.result != null) {
          await _showApplyRecognitionDialog(poll.result!);
          break;
        }
        if (poll.status == 'failed') {
          if (mounted) {
            AppFeedback.showToast(
              context,
              kind: FeedbackToastKind.failure,
              title: poll.errorMessage?.isNotEmpty == true
                  ? poll.errorMessage!
                  : '识别失败（${poll.errorCode ?? '-'}）',
            );
          }
          break;
        }
      }
    } on ApiBusinessException catch (e) {
      if (mounted) {
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: e.message.isNotEmpty ? e.message : '识别任务失败（${e.code}）',
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
      if (mounted && session == _aiSession) {
        setState(() => _aiRecognizing = false);
      }
    }
  }

  Future<void> _showApplyRecognitionDialog(DishIngredientVisionData data) async {
    final rec = data.recognition;
    final ingredients = rec.ingredients ?? <VisionIngredient>[];
    final dish = rec.dish;
    if (!mounted) return;
    final apply = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (ctx) {
        final ingredientLine = ingredients.isEmpty
            ? ''
            : ingredients
                .map((e) {
                  final n = e.foodName.trim();
                  return n.isNotEmpty ? n : '食物 #${e.ingredientId}';
                })
                .join('、');
        return AlertDialog(
          title: const Text('识别完成'),
          content: SingleChildScrollView(
            child: Text(
              ingredients.isEmpty && dish == null
                  ? '未匹配到菜品或食材，可手动添加食物。'
                  : ingredients.isEmpty
                      ? '已匹配菜品，未识别到具体食材，可将菜品信息写入保存请求或手动添加食物。'
                      : '识别到食材：$ingredientLine\n\n是否加入当前列表？',
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('暂不应用'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('应用'),
            ),
          ],
        );
      },
    );
    if (!mounted || apply != true) return;
    setState(() {
      if (dish != null) {
        _matchedDishId = int.tryParse(dish.dishId);
        _dishMatchSource = dish.match?.via;
        _dishMatchConfidence = dish.confidence;
        _matchedDishName = null;
      }
      for (final ing in ingredients) {
        final id = int.tryParse(ing.ingredientId);
        if (id != null) {
          _foods.add(
            DraftFoodItem.aiFromVision(
              foodItemId: id,
              foodName: ing.foodName,
              caloriesPer100g: ing.caloriesPer100g,
            ),
          );
        }
      }
      _photoAiCompleted = dish != null || ingredients.isNotEmpty;
    });
  }

  void _interruptAi() {
    _recognitionPollCancelled = true;
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
              Positioned(
                left: 0,
                right: 0,
                top: 0,
                child: SafeArea(
                  bottom: false,
                  child: Material(
                    elevation: 3,
                    color: AppColors.bgPrimary,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.s16,
                        vertical: AppSpacing.s12,
                      ),
                      child: Row(
                        children: [
                          const SizedBox(
                            width: 22,
                            height: 22,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: AppColors.primary,
                            ),
                          ),
                          const SizedBox(width: AppSpacing.s12),
                          Expanded(
                            child: Text(
                              'AI 识别中（约 60 秒内完成，不影响保存）',
                              style: AppTypography.bodySmall(color: AppColors.textSecondary),
                            ),
                          ),
                          TextButton(
                            onPressed: _interruptAi,
                            child: Text(
                              '取消',
                              style: AppTypography.buttonText(color: AppColors.primary),
                            ),
                          ),
                        ],
                      ),
                    ),
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

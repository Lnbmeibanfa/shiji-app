import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';
import '../models/draft_food_item.dart';
import '../models/food_item_page.dart';

/// 设计稿：标题、搜索、分页列表；选中一行后 `pop` [DraftFoodItem]。
class AddFoodBottomSheet extends ConsumerStatefulWidget {
  const AddFoodBottomSheet({super.key});

  @override
  ConsumerState<AddFoodBottomSheet> createState() => _AddFoodBottomSheetState();
}

class _AddFoodBottomSheetState extends ConsumerState<AddFoodBottomSheet> {
  final TextEditingController _search = TextEditingController();
  final ScrollController _scroll = ScrollController();

  Timer? _debounce;
  List<FoodItemSummary> _items = [];
  int _page = 0;
  bool _loading = false;
  bool _loadingMore = false;
  bool _hasNext = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _search.addListener(_onSearchChanged);
    _scroll.addListener(_onScroll);
    WidgetsBinding.instance.addPostFrameCallback((_) => _fetch());
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _search.removeListener(_onSearchChanged);
    _scroll.removeListener(_onScroll);
    _search.dispose();
    _scroll.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (!_scroll.hasClients) return;
    if (_scroll.position.extentAfter > 160) return;
    if (!_hasNext || _loading || _loadingMore) return;
    _fetch(append: true);
  }

  void _onSearchChanged() {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), () {
      if (mounted) _fetch();
    });
  }

  Future<void> _fetch({bool append = false}) async {
    if (_loading || _loadingMore) return;
    final q = _search.text.trim();
    final nextPage = append ? _page + 1 : 0;

    setState(() {
      _error = null;
      if (append) {
        _loadingMore = true;
      } else {
        _loading = true;
      }
    });

    try {
      final repo = ref.read(foodItemRepositoryProvider);
      final res = await repo.search(
        q: q.isEmpty ? null : q,
        page: nextPage,
        size: 20,
      );
      if (!mounted) return;
      setState(() {
        if (append) {
          _items = [..._items, ...res.items];
          _page = nextPage;
        } else {
          _items = res.items;
          _page = nextPage;
        }
        _hasNext = res.hasNext;
      });
    } catch (e) {
      if (mounted) {
        setState(() => _error = e.toString());
      }
    } finally {
      if (mounted) {
        setState(() {
          _loading = false;
          _loadingMore = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.viewInsetsOf(context).bottom;
    final h = MediaQuery.sizeOf(context).height * 0.85;

    return Padding(
      padding: EdgeInsets.only(bottom: bottom),
      child: SizedBox(
        height: h,
        child: DecoratedBox(
          decoration: const BoxDecoration(
            color: AppColors.bgPrimary,
            borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.xl)),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(
                  AppSpacing.s24,
                  AppSpacing.s16,
                  AppSpacing.s8,
                  AppSpacing.s8,
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        '添加食物',
                        textAlign: TextAlign.center,
                        style: AppTypography.titleMedium(color: AppColors.textPrimary),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close_rounded),
                      color: AppColors.textSecondary,
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: AppSpacing.s24),
                child: TextField(
                  controller: _search,
                  decoration: InputDecoration(
                    hintText: '搜索食物...',
                    hintStyle: AppTypography.bodyMedium(color: AppColors.textTertiary),
                    prefixIcon: const Icon(Icons.search_rounded, color: AppColors.textTertiary),
                    filled: true,
                    fillColor: AppColors.bgMuted,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(AppRadius.pill),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.s16,
                      vertical: AppSpacing.s12,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.s12),
              Expanded(child: _buildList()),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildList() {
    if (_loading && _items.isEmpty) {
      return const Center(child: CircularProgressIndicator(color: AppColors.primary));
    }
    if (_error != null && _items.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.s24),
          child: Text(
            '加载失败：$_error',
            textAlign: TextAlign.center,
            style: AppTypography.bodyMedium(color: AppColors.textSecondary),
          ),
        ),
      );
    }
    if (_items.isEmpty) {
      return Center(
        child: Text(
          '暂无食物',
          style: AppTypography.bodyMedium(color: AppColors.textTertiary),
        ),
      );
    }

    return ListView.builder(
      controller: _scroll,
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.s24),
      itemCount: _items.length + (_loadingMore ? 1 : 0),
      itemBuilder: (context, i) {
        if (i >= _items.length) {
          return const Padding(
            padding: EdgeInsets.all(AppSpacing.s16),
            child: Center(
              child: SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: AppColors.primary,
                ),
              ),
            ),
          );
        }
        final item = _items[i];
        final kcal = item.caloriesPer100g;
        final sub = kcal != null ? '${kcal.toStringAsFixed(0)} kcal/100g' : '--';

        return Padding(
          padding: const EdgeInsets.only(bottom: AppSpacing.s8),
          child: Material(
            color: AppColors.bgMuted,
            borderRadius: BorderRadius.circular(AppRadius.md),
            child: InkWell(
              borderRadius: BorderRadius.circular(AppRadius.md),
              onTap: () {
                Navigator.of(context).pop(DraftFoodItem.fromSearch(item));
              },
              child: Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.s16,
                  vertical: AppSpacing.s12,
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        item.foodName,
                        style: AppTypography.bodyLarge(color: AppColors.textPrimary),
                      ),
                    ),
                    Text(
                      sub,
                      style: AppTypography.bodySmall(color: AppColors.textSecondary),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}

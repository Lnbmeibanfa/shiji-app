import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

/// 底部四 Tab 主壳：内容与 [StatefulNavigationShell] 分支同步。
class MainShellPage extends StatelessWidget {
  const MainShellPage({
    super.key,
    required this.navigationShell,
  });

  final StatefulNavigationShell navigationShell;

  static const _labels = ['首页', '记录', '复盘', '我的'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: NavigationBarTheme(
        data: NavigationBarThemeData(
          indicatorColor:
              AppColors.navTabSelected.withValues(alpha: 0.28),
          iconTheme: WidgetStateProperty.resolveWith((states) {
            if (states.contains(WidgetState.selected)) {
              return const IconThemeData(
                color: AppColors.navTabSelected,
                size: 24,
              );
            }
            return IconThemeData(color: AppColors.textTertiary, size: 24);
          }),
          labelTextStyle: WidgetStateProperty.resolveWith((states) {
            if (states.contains(WidgetState.selected)) {
              return AppTypography.labelMedium(color: AppColors.navTabSelected);
            }
            return AppTypography.labelMedium(color: AppColors.textTertiary);
          }),
        ),
        child: NavigationBar(
          selectedIndex: navigationShell.currentIndex,
          onDestinationSelected: (index) {
            navigationShell.goBranch(
              index,
              initialLocation: index == navigationShell.currentIndex,
            );
          },
          destinations: [
            NavigationDestination(
              icon: const Icon(Icons.home_outlined),
              label: _labels[0],
            ),
            NavigationDestination(
              icon: const Icon(Icons.calendar_month_outlined),
              label: _labels[1],
            ),
            NavigationDestination(
              icon: const Icon(Icons.history_outlined),
              label: _labels[2],
            ),
            NavigationDestination(
              icon: const Icon(Icons.person_outline_rounded),
              label: _labels[3],
            ),
          ],
        ),
      ),
    );
  }
}

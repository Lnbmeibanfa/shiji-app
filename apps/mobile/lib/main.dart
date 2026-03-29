import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/providers.dart';
import 'core/theme/shiji_theme.dart';

/// 食迹移动端应用入口
void main() {
  runApp(const ProviderScope(child: ShijiApp()));
}

/// 根部件，使用 [MaterialApp.router] 与 [GoRouter]。
class ShijiApp extends ConsumerWidget {
  const ShijiApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    return MaterialApp.router(
      title: '食迹',
      theme: buildShijiTheme(),
      routerConfig: router,
    );
  }
}

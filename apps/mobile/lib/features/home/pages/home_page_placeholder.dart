import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/providers.dart';

/// 首页占位页面（已登录后进入）。
///
/// 提供「退出登录」便于联调会话与路由；真实首页由后续 OpenSpec 实现。
class HomePagePlaceholder extends ConsumerWidget {
  const HomePagePlaceholder({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('首页')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text(
              '首页占位页面（features/home）',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            TextButton(
              onPressed: () async {
                await ref.read(authControllerProvider).signOut();
              },
              child: const Text('退出登录'),
            ),
          ],
        ),
      ),
    );
  }
}

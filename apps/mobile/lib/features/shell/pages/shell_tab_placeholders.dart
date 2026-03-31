import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/providers.dart';

/// 首页 Tab — 文字占位。
class HomeTabPlaceholder extends StatelessWidget {
  const HomeTabPlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Text('首页占位'),
    );
  }
}

/// 记录 Tab — 文字占位。
class RecordTabPlaceholder extends StatelessWidget {
  const RecordTabPlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Text('记录占位'),
    );
  }
}

/// 复盘 Tab — 文字占位。
class ReviewTabPlaceholder extends StatelessWidget {
  const ReviewTabPlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Text('复盘占位'),
    );
  }
}

/// 我的 Tab — 文字占位 + 退出登录（联调会话）。
class ProfileTabPlaceholder extends ConsumerWidget {
  const ProfileTabPlaceholder({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('我的占位'),
          const SizedBox(height: 24),
          TextButton(
            onPressed: () async {
              await ref.read(authControllerProvider).signOut();
            },
            child: const Text('退出登录'),
          ),
        ],
      ),
    );
  }
}

import 'package:flutter/material.dart';

/// 首页占位页面。
///
/// 实际实现需先在 OpenSpec 中定义对应业务规范与用例。
class HomePagePlaceholder extends StatelessWidget {
  const HomePagePlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text(
          '首页占位页面（features/home）',
          textAlign: TextAlign.center,
        ),
      ),
    );
  }
}


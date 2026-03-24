import 'package:flutter/material.dart';

import 'core/theme/shiji_theme.dart';

/// 食迹移动端应用入口
void main() {
  runApp(const ShijiApp());
}

/// 根部件，后续会接入真正的路由和页面
class ShijiApp extends StatelessWidget {
  const ShijiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '食迹',
      theme: buildShijiTheme(),
      home: const _AppPlaceholderPage(),
    );
  }
}

/// 占位首页，提示当前项目结构
class _AppPlaceholderPage extends StatelessWidget {
  const _AppPlaceholderPage();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text(
          '食迹（shiji）移动端\n\n'
          '当前仅为占位页面，\n'
          '实际页面将从 features/ 下的业务模块接入。',
          textAlign: TextAlign.center,
        ),
      ),
    );
  }
}

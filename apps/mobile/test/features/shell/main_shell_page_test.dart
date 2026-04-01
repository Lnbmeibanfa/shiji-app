import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/core/providers.dart';
import 'package:mobile/core/theme/shiji_theme.dart';

void main() {
  testWidgets('主壳：切换 Tab 后显示对应占位文案', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          authControllerProvider.overrideWith((ref) {
            return AuthController.authenticatedForTest(
              ref.watch(authStorageProvider),
            );
          }),
        ],
        child: Consumer(
          builder: (context, ref, _) {
            final router = ref.watch(routerProvider);
            return MaterialApp.router(
              theme: buildShijiTheme(),
              routerConfig: router,
            );
          },
        ),
      ),
    );

    await tester.pumpAndSettle();

    expect(find.text('今天也一起轻松吃饭吧'), findsOneWidget);
    expect(find.text('最近记录'), findsOneWidget);

    await tester.tap(find.text('查看全部'));
    await tester.pumpAndSettle();
    expect(find.text('记录占位'), findsOneWidget);

    await tester.tap(find.text('首页'));
    await tester.pumpAndSettle();
    expect(find.text('今天也一起轻松吃饭吧'), findsOneWidget);

    await tester.tap(find.text('记录'));
    await tester.pumpAndSettle();
    expect(find.text('记录占位'), findsOneWidget);

    await tester.tap(find.text('复盘'));
    await tester.pumpAndSettle();
    expect(find.text('复盘占位'), findsOneWidget);

    await tester.tap(find.text('我的'));
    await tester.pumpAndSettle();
    expect(find.text('我的占位'), findsOneWidget);
  });
}

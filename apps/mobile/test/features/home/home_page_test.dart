import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/network/models/latest_meal_record_response.dart';
import 'package:mobile/core/providers.dart';
import 'package:mobile/core/routing/route_paths.dart';
import 'package:mobile/features/home/pages/home_page.dart';
import 'package:mobile/features/home/repositories/home_repository.dart';

void main() {
  group('HomePage latest meal card', () {
    testWidgets('进入首页触发最近一餐请求', (tester) async {
      final completer = Completer<LatestMealRecordResponse?>();
      final fakeRepo = _FakeHomeRepository(() => completer.future);
      await _pumpHomeWithRouter(
        tester,
        fakeRepo: fakeRepo,
      );

      expect(fakeRepo.callCount, 1);
      completer.complete(null);
      await tester.pumpAndSettle();
    });

    testWidgets('有数据时展示餐别+时间+整数热量', (tester) async {
      final fakeRepo = _FakeHomeRepository(() async {
        return LatestMealRecordResponse(
          mealType: 'lunch',
          recordedAt: DateTime(2026, 4, 28, 12, 30),
          totalEstimatedCalories: 520.6,
          mood: null,
        );
      });
      await _pumpHomeWithRouter(
        tester,
        fakeRepo: fakeRepo,
      );
      await tester.pumpAndSettle();

      expect(find.text('午餐 12:30'), findsOneWidget);
      expect(find.text('521 kcal'), findsOneWidget);
    });

    testWidgets('data=null 时展示引导并支持跳转', (tester) async {
      final fakeRepo = _FakeHomeRepository(() async => null);
      await _pumpHomeWithRouter(
        tester,
        fakeRepo: fakeRepo,
      );
      await tester.pumpAndSettle();

      expect(find.text('去记录一餐'), findsOneWidget);
      await tester.tap(find.text('去记录一餐'));
      await tester.pumpAndSettle();
      expect(find.text('记录饮食页'), findsOneWidget);
    });

    testWidgets('接口失败时展示降级内容', (tester) async {
      final fakeRepo = _FakeHomeRepository(() async {
        throw Exception('network');
      });
      await _pumpHomeWithRouter(
        tester,
        fakeRepo: fakeRepo,
      );
      await tester.pumpAndSettle();

      expect(find.text('加载失败，稍后再试'), findsOneWidget);
      expect(find.text('拍一顿'), findsOneWidget);
    });
  });
}

Future<void> _pumpHomeWithRouter(
  WidgetTester tester, {
  required _FakeHomeRepository fakeRepo,
}) async {
  final router = GoRouter(
    initialLocation: RoutePaths.home,
    routes: <RouteBase>[
      GoRoute(
        path: RoutePaths.home,
        builder: (context, state) => const HomePage(),
      ),
      GoRoute(
        path: RoutePaths.recordMeal,
        builder: (context, state) {
          return const Scaffold(body: Text('记录饮食页'));
        },
      ),
    ],
  );

  await tester.pumpWidget(
    ProviderScope(
      overrides: <Override>[
        homeRepositoryProvider.overrideWithValue(fakeRepo),
      ],
      child: MaterialApp.router(
        routerConfig: router,
      ),
    ),
  );
}

class _FakeHomeRepository extends HomeRepository {
  _FakeHomeRepository(this._loader) : super(ApiClient(Dio()));

  final Future<LatestMealRecordResponse?> Function() _loader;
  int callCount = 0;

  @override
  Future<LatestMealRecordResponse?> getLatestMeal() {
    callCount++;
    return _loader();
  }
}

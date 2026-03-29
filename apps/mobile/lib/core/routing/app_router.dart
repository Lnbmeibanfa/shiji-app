import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../auth/auth_controller.dart';
import '../../features/auth/pages/login_page.dart';
import '../../features/home/pages/home_page_placeholder.dart';
import '../../features/splash/pages/splash_page.dart';
import 'route_paths.dart';

GoRouter createAppRouter(AuthController auth) {
  return GoRouter(
    initialLocation: RoutePaths.splash,
    refreshListenable: auth,
    redirect: (BuildContext context, GoRouterState state) {
      final ac = auth;
      final loc = state.matchedLocation;

      if (!ac.isReady) {
        if (loc != RoutePaths.splash) {
          return RoutePaths.splash;
        }
        return null;
      }

      if (loc == RoutePaths.splash) {
        return ac.isAuthenticated ? RoutePaths.home : RoutePaths.login;
      }

      final onLogin = loc == RoutePaths.login;
      if (!ac.isAuthenticated && loc != RoutePaths.login) {
        return RoutePaths.login;
      }
      if (ac.isAuthenticated && onLogin) {
        return RoutePaths.home;
      }
      return null;
    },
    routes: <RouteBase>[
      GoRoute(
        path: RoutePaths.splash,
        builder: (context, state) => const SplashPage(),
      ),
      GoRoute(
        path: RoutePaths.login,
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: RoutePaths.home,
        builder: (context, state) => const HomePagePlaceholder(),
      ),
    ],
  );
}

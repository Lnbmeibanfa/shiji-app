import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../auth/auth_controller.dart';
import '../../features/auth/pages/login_page.dart';
import '../../features/shell/pages/main_shell_page.dart';
import '../../features/shell/pages/shell_tab_placeholders.dart';
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
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) {
          return MainShellPage(navigationShell: navigationShell);
        },
        branches: <StatefulShellBranch>[
          StatefulShellBranch(
            routes: <RouteBase>[
              GoRoute(
                path: RoutePaths.home,
                builder: (context, state) => const HomeTabPlaceholder(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: <RouteBase>[
              GoRoute(
                path: RoutePaths.record,
                builder: (context, state) => const RecordTabPlaceholder(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: <RouteBase>[
              GoRoute(
                path: RoutePaths.review,
                builder: (context, state) => const ReviewTabPlaceholder(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: <RouteBase>[
              GoRoute(
                path: RoutePaths.profile,
                builder: (context, state) => const ProfileTabPlaceholder(),
              ),
            ],
          ),
        ],
      ),
    ],
  );
}

#!/usr/bin/env bash
# 扫描 Flutter features 目录中的 Color(0x...) 字面量，提醒改用 AppColors。
# 用法：在仓库根目录执行 bash scripts/check_flutter_color_literals.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="$ROOT/apps/mobile/lib/features"

if ! command -v rg >/dev/null 2>&1; then
  echo "未找到 rg（ripgrep），请先安装：https://github.com/BurntSushi/ripgrep"
  exit 1
fi

if [[ ! -d "$TARGET" ]]; then
  echo "目录不存在：$TARGET"
  exit 0
fi

echo "扫描：$TARGET"
if rg 'Color\(0x' "$TARGET" --glob '*.dart'; then
  echo ""
  echo "发现上述命中：请在业务代码中使用 AppColors（定义见 apps/mobile/lib/core/theme/app_colors.dart）。"
  exit 1
fi

echo "未发现 Color(0x...) 字面量（features）。"

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/feedback/app_feedback.dart';
import '../../../core/network/api_exceptions.dart';
import '../../../core/network/models/file_upload_response.dart';
import '../../../core/providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_radius.dart';
import '../../../core/theme/app_spacing.dart';
import '../../../core/theme/app_typography.dart';

/// 选图 / 拍照、上传、预览、清除；上传结果通过 [onUploadedChanged] 交给父级。
class RecordMealPhotoSection extends ConsumerStatefulWidget {
  const RecordMealPhotoSection({
    super.key,
    required this.onUploadedChanged,
    this.onUploadingChanged,
  });

  final ValueChanged<FileUploadResponse?> onUploadedChanged;
  final ValueChanged<bool>? onUploadingChanged;

  @override
  ConsumerState<RecordMealPhotoSection> createState() =>
      _RecordMealPhotoSectionState();
}

class _RecordMealPhotoSectionState extends ConsumerState<RecordMealPhotoSection> {
  final ImagePicker _picker = ImagePicker();

  XFile? _picked;
  FileUploadResponse? _uploaded;
  bool _uploading = false;
  bool _uploadFailed = false;

  Future<void> _pick(ImageSource source) async {
    final x = await _picker.pickImage(source: source);
    if (!mounted || x == null) return;
    setState(() {
      _picked = x;
      _uploaded = null;
      _uploadFailed = false;
    });
    widget.onUploadedChanged(null);
    await _uploadFile(x);
  }

  Future<void> _uploadFile(XFile file) async {
    if (_uploading) return;

    setState(() {
      _uploading = true;
      _uploadFailed = false;
    });
    widget.onUploadingChanged?.call(true);
    try {
      final res =
          await ref.read(mealPhotoRepositoryProvider).uploadMealPhoto(file);
      if (!mounted) return;
      setState(() {
        _uploaded = res;
        _uploadFailed = false;
      });
      widget.onUploadedChanged(res);
    } on ApiBusinessException catch (e) {
      if (mounted) {
        setState(() => _uploadFailed = true);
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: e.message.isNotEmpty ? e.message : '上传失败（${e.code}）',
        );
      }
      widget.onUploadedChanged(null);
    } on ApiHttpException catch (e) {
      if (mounted) {
        setState(() => _uploadFailed = true);
        AppFeedback.showToast(
          context,
          kind: FeedbackToastKind.failure,
          title: e.message ?? '网络异常',
        );
      }
      widget.onUploadedChanged(null);
    } finally {
      if (mounted) {
        setState(() => _uploading = false);
        widget.onUploadingChanged?.call(false);
      }
    }
  }

  void _clear() {
    setState(() {
      _picked = null;
      _uploaded = null;
      _uploadFailed = false;
    });
    widget.onUploadedChanged(null);
  }

  @override
  Widget build(BuildContext context) {
    final previewUrl = _uploaded?.url;
    final hasRemote = previewUrl != null && previewUrl.isNotEmpty;
    final previewHeight = MediaQuery.sizeOf(context).height / 3;

    return SizedBox(
      height: previewHeight,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(AppRadius.lg),
        child: ColoredBox(
          color: AppColors.bgMuted,
          child: Stack(
            fit: StackFit.expand,
            children: [
              if (hasRemote)
                Image.network(
                  previewUrl,
                  fit: BoxFit.cover,
                  loadingBuilder: (context, child, progress) {
                    if (progress == null) return child;
                    return const Center(
                      child: CircularProgressIndicator(
                        color: AppColors.primary,
                      ),
                    );
                  },
                  errorBuilder: (context, error, stackTrace) => const Center(
                    child: Icon(
                      Icons.broken_image_outlined,
                      size: 48,
                      color: AppColors.textTertiary,
                    ),
                  ),
                )
              else if (_picked != null)
                FutureBuilder<Uint8List>(
                  future: _picked!.readAsBytes(),
                  builder: (context, snap) {
                    if (snap.hasError) {
                      return const Center(
                        child: Icon(
                          Icons.broken_image_outlined,
                          size: 48,
                          color: AppColors.textTertiary,
                        ),
                      );
                    }
                    if (!snap.hasData) {
                      return const Center(
                        child: CircularProgressIndicator(
                          color: AppColors.primary,
                        ),
                      );
                    }
                    return Image.memory(
                      snap.data!,
                      fit: BoxFit.cover,
                    );
                  },
                )
              else
                Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Material(
                        color: AppColors.primary,
                        shape: const CircleBorder(),
                        child: InkWell(
                          customBorder: const CircleBorder(),
                          onTap: _uploading
                              ? null
                              : () => _pick(ImageSource.camera),
                          child: const Padding(
                            padding: EdgeInsets.all(AppSpacing.s20),
                            child: Icon(
                              Icons.photo_camera_rounded,
                              size: 36,
                              color: AppColors.textInverse,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: AppSpacing.s16),
                      TextButton.icon(
                        onPressed:
                            _uploading ? null : () => _pick(ImageSource.gallery),
                        icon: const Icon(
                          Icons.photo_library_outlined,
                          color: AppColors.primary,
                        ),
                        label: Text(
                          '相册',
                          style: AppTypography.bodyMedium(
                            color: AppColors.primary,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              if (hasRemote || (_picked != null && !_uploading))
                Positioned(
                  top: AppSpacing.s12,
                  right: AppSpacing.s12,
                  child: Material(
                    color: AppColors.bgCard.withValues(alpha: 0.9),
                    shape: const CircleBorder(),
                    child: IconButton(
                      icon: const Icon(Icons.close_rounded),
                      color: AppColors.textSecondary,
                      onPressed: _uploading ? null : _clear,
                    ),
                  ),
                ),
              if (_uploadFailed &&
                  _picked != null &&
                  !_uploading &&
                  !hasRemote)
                Positioned(
                  bottom: AppSpacing.s16,
                  left: AppSpacing.s16,
                  right: AppSpacing.s16,
                  child: FilledButton(
                    onPressed: () => _uploadFile(_picked!),
                    style: FilledButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      foregroundColor: AppColors.textInverse,
                    ),
                    child: const Text('重试上传'),
                  ),
                ),
              if (_uploading)
                const ColoredBox(
                  color: Color(0x66F6F7F4),
                  child: Center(
                    child: CircularProgressIndicator(
                      color: AppColors.primary,
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

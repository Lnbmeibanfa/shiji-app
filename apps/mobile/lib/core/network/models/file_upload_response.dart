/// 与后端 `FileUploadResponse` 对齐。
class FileUploadResponse {
  const FileUploadResponse({
    required this.fileId,
    required this.url,
    required this.objectKey,
    required this.bucket,
    required this.contentType,
    required this.size,
  });

  final int fileId;
  final String url;
  final String objectKey;
  final String bucket;
  final String contentType;
  final int size;

  factory FileUploadResponse.fromJson(Object? raw) {
    if (raw is! Map<String, dynamic>) {
      throw FormatException('FileUploadResponse: data 不是对象');
    }
    return FileUploadResponse(
      fileId: (raw['fileId'] as num).toInt(),
      url: raw['url'] as String? ?? '',
      objectKey: raw['objectKey'] as String? ?? '',
      bucket: raw['bucket'] as String? ?? '',
      contentType: raw['contentType'] as String? ?? '',
      size: (raw['size'] as num?)?.toInt() ?? 0,
    );
  }
}

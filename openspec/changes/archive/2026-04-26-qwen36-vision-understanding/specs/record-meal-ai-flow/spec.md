## MODIFIED Requirements

### Requirement: 上传图片后使用 stub 跑通识别流程
在接入真实 AI 后，当用户上传图片并成功得到 `file_id`（或等价图片引用）后，应用 MUST 进入 `aiRecognizing`，并调用视觉理解接口（默认模型 `qwen3.6-flash`）获取识别结果；在识别成功时 MUST 向列表追加识别出的食物行并完成 `uploading → aiRecognizing → idle → 保存` 流程。若调用失败或超时，应用 MUST 回到 `idle`，并 MUST 提供可见失败提示与可重试入口；在失败场景下系统 MUST NOT 追加伪造识别结果。

#### Scenario: 真实识别成功后可保存
- **WHEN** 图片上传成功且视觉识别接口返回可用结果，列表至少有一行
- **THEN** 保存按钮 MUST 可点击，且 `recordMethod` MUST 符合 `photo_ai` 约定

#### Scenario: 真实识别失败时回退
- **WHEN** 视觉识别接口返回错误或超时
- **THEN** 页面状态 MUST 回到 `idle`，MUST 显示失败提示与重试入口，且 MUST NOT 向列表追加占位 stub 行

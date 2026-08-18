<template>
  <div class="image-upload">
    <el-upload
      class="image-uploader"
      :show-file-list="false"
      :on-success="handleSuccess"
      :before-upload="beforeUpload"
      :action="uploadUrl"
      :accept="accept"
      :headers="headers"
    >
      <img v-if="imageUrl" :src="imageUrl" class="image-preview" />
      <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
    </el-upload>
    <div v-if="tip" class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getToken } from '../utils/auth';

/**
 * 通用图片上传组件
 * 支持上传到阿里云OSS，自动预览
 */
const props = defineProps({
  // 上传接口地址
  uploadUrl: {
    type: String,
    default: '/api/upload/avatar'
  },
  // 当前图片URL（用于回显）
  modelValue: {
    type: String,
    default: ''
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: '.png,.jpg,.jpeg,.webp'
  },
  // 提示文字
  tip: {
    type: String,
    default: ''
  },
  // 最大文件大小（MB）
  maxSize: {
    type: Number,
    default: 2
  }
});

const emit = defineEmits(['update:modelValue', 'success']);

const imageUrl = ref(props.modelValue);

// 监听外部modelValue变化
watch(() => props.modelValue, (newVal) => {
  imageUrl.value = newVal;
});

// 请求头（携带token）
const headers = {
  Authorization: 'Bearer ' + getToken()
};

/**
 * 上传前校验
 */
function beforeUpload(file) {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg' || file.type === 'image/webp';
  const isLtSize = file.size / 1024 / 1024 < props.maxSize;

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG/WEBP 格式的图片!');
    return false;
  }
  if (!isLtSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB!`);
    return false;
  }
  return true;
}

/**
 * 上传成功回调
 */
function handleSuccess(response) {
  if (response.code === 200) {
    imageUrl.value = response.data;
    emit('update:modelValue', response.data);
    emit('success', response.data);
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(response.message || '图片上传失败');
  }
}
</script>

<style scoped lang="less">
.image-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--theme-border);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--theme-transition);

    &:hover {
      border-color: var(--theme-primary);
    }
  }
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  line-height: 148px;
  text-align: center;
}

.image-preview {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--theme-text-muted);
  text-align: center;
}
</style>

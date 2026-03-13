<template>
  <div class="upload-container">
    <el-upload
      v-if="!multiple"
      class="image-uploader"
      action="#"
      :http-request="uploadFile"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :disabled="disabled"
    >
      <img v-if="modelValue" :src="modelValue" class="uploaded-image" />
      <el-icon v-else class="uploader-icon"><Plus /></el-icon>
      
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
    </el-upload>

    <el-upload
      v-else
      v-model:file-list="fileList"
      list-type="picture-card"
      action="#"
      :http-request="uploadFile"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :disabled="disabled"
      :multiple="true"
      :limit="maxCount"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <div v-if="tip" class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { Plus, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'common' // avatar, product, cert
  },
  limit: {
    type: Number,
    default: 5 // MB
  },
  disabled: {
    type: Boolean,
    default: false
  },
  tip: {
    type: String,
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  },
  maxCount: {
    type: Number,
    default: 9
  }
})

const emit = defineEmits(['update:modelValue', 'success'])
const loading = ref(false)
const fileList = ref([])

// Initialize fileList from modelValue if multiple is true
const initFileList = () => {
  if (props.multiple && props.modelValue) {
    fileList.value = props.modelValue.split(',').map((url, index) => ({
      name: `Image ${index + 1}`,
      url: url.trim()
    }))
  } else {
    fileList.value = []
  }
}

watch(() => props.modelValue, (val) => {
  if (props.multiple) {
    const newUrls = val ? val.split(',') : []
    
    // Get current remote URLs from fileList to compare
    const currentRemoteUrls = fileList.value
      .filter(f => f.url && f.url.startsWith('http'))
      .map(f => f.url)
    
    // If the remote URLs match, don't mess with fileList (preserves uploading files)
    if (newUrls.join(',') === currentRemoteUrls.join(',')) {
      return
    }

    // If they differ, we need to sync, but preserve uploading files (blobs)
    const uploadingFiles = fileList.value.filter(f => !f.url || !f.url.startsWith('http'))
    
    const newFiles = newUrls.map((url, index) => ({
      name: `Image ${index + 1}`,
      url: url.trim(),
      status: 'success',
      uid: Date.now() + index // Ensure unique ID for new items
    }))
    
    fileList.value = [...newFiles, ...uploadingFiles]
  }
})

onMounted(() => {
  initFileList()
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isPDF = file.type === 'application/pdf'
  
  if (!isImage && !isPDF) {
    ElMessage.error('只能上传图片或PDF文件!')
    return false
  }
  
  const isLtLimit = file.size / 1024 / 1024 < props.limit
  if (!isLtLimit) {
    ElMessage.error(`上传文件大小不能超过 ${props.limit}MB!`)
    return false
  }
  return true
}

const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.maxCount} 张图片`)
}

const handleRemove = (uploadFile, uploadFiles) => {
  // Update modelValue when a file is removed
  const urls = uploadFiles.map(f => f.url).join(',')
  emit('update:modelValue', urls)
}

const uploadFile = async (options) => {
  loading.value = true
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('type', props.type)

  try {
    const res = await request({
      url: '/files/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (res && (res.url || (typeof res === 'string' && res.includes('http')))) {
      const url = res.url || res
      
      if (props.multiple) {
        // Add to fileList
        // Note: Element Plus upload component automatically adds the file object to the list,
        // but we need to update its url property from blob to remote url
        const fileIndex = fileList.value.findIndex(f => f.uid === options.file.uid)
        if (fileIndex > -1) {
            fileList.value[fileIndex].url = url
        } else {
             // Fallback if not found by uid (shouldn't happen with standard usage)
             // But actually, for custom http-request, we might need to manually manage
             // Element Plus adds a 'ready' file, we need to make sure we have the correct structure
             // The safest way for multiple mode with custom http-request is often to manually push
             // However, v-model:file-list handles the UI state.
             // We just need to update the URL of the last added file or reload list
             
             // Simplest approach: Just update the modelValue. 
             // Since we watch modelValue, fileList will update? 
             // No, watching modelValue -> fileList might cause re-render issues during upload.
             // Better to just emit the new string.
        }
        
        // Let's rely on fileList being up to date with the new file (which has a blob url initially)
        // We replace that blob url with the real url
        // Actually, 'options.onSuccess' is standard way but we are manual.
        
        // Find the file in fileList that matches the one we just uploaded
        // options.file is the raw file. 
        // fileList elements have .raw property pointing to it.
        const uploadedFileItem = fileList.value.find(f => f.uid === options.file.uid)
        if (uploadedFileItem) {
            uploadedFileItem.url = url
            uploadedFileItem.status = 'success'
        }
        
        // Emit new comma separated string
        const urls = fileList.value.map(f => f.url).filter(u => u && u.startsWith('http')).join(',')
        emit('update:modelValue', urls)
        
      } else {
        emit('update:modelValue', url)
      }
      
      emit('success', res)
      ElMessage.success('上传成功')
    } else {
      console.error('Upload response invalid:', res)
      throw new Error('上传返回格式错误')
    }

  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '上传失败')
    if (props.multiple) {
        // Remove failed file from list
        const idx = fileList.value.findIndex(f => f.uid === options.file.uid)
        if (idx > -1) fileList.value.splice(idx, 1)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.upload-container {
  display: inline-block;
  vertical-align: top;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 148px;
  height: 148px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: var(--el-transition-duration-fast);
}

.image-uploader:hover {
  border-color: #409eff;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  text-align: center;
  line-height: 148px;
}

.uploaded-image {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

/* Deep selector for multiple upload list */
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 148px;
  height: 148px;
}
:deep(.el-upload--picture-card) {
  width: 148px;
  height: 148px;
}
</style>

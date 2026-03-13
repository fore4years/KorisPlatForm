<template>
  <el-dialog
    v-model="visible"
    title="电子租赁合同"
    width="800px"
    :before-close="handleClose"
  >
    <div class="contract-content" v-loading="loading">
      <div v-if="contract" v-html="contract.content"></div>
      <el-empty v-else description="合同生成中..." />
      
      <div class="signature-section" v-if="contract">
        <div class="sign-box">
          <p>出租方签署：</p>
          <el-tag v-if="contract.merchantSigned" type="success">已签署</el-tag>
          <span v-else>待签署</span>
        </div>
        <div class="sign-box">
          <p>承租方签署：</p>
          <el-tag v-if="contract.tenantSigned" type="success">已签署</el-tag>
          <span v-else>待签署</span>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleSign" 
          :loading="signing"
          :disabled="!canSign"
        >
          {{ signed ? '已签署' : '同意并签署' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getContract, signContract } from '../api/contract'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  orderId: [Number, String],
  userRole: String, // 'TENANT' or 'MERCHANT'
  userId: [Number, String]
})

const emit = defineEmits(['update:modelValue', 'signed'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const signing = ref(false)
const contract = ref(null)

const signed = computed(() => {
  if (!contract.value) return false
  return props.userRole === 'TENANT' ? contract.value.tenantSigned : contract.value.merchantSigned
})

const canSign = computed(() => {
  return contract.value && !signed.value
})

const loadContract = async () => {
  if (!props.orderId) return
  loading.value = true
  try {
    const res = await getContract(props.orderId)
    contract.value = res
  } catch (error) {
    ElMessage.error('获取合同失败')
  } finally {
    loading.value = false
  }
}

const handleSign = async () => {
  if (!canSign.value) return
  signing.value = true
  try {
    await signContract(props.orderId, props.userId, props.userRole)
    ElMessage.success('签署成功')
    await loadContract()
    emit('signed')
  } catch (error) {
    ElMessage.error('签署失败')
  } finally {
    signing.value = false
  }
}

const handleClose = () => {
  visible.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) {
    loadContract()
  }
})
</script>

<style scoped>
.contract-content {
  max-height: 500px;
  overflow-y: auto;
  padding: 20px;
  border: 1px solid #eee;
  background: #fdfdfd;
}
.signature-section {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #ddd;
  padding-top: 20px;
}
.sign-box {
  text-align: center;
}
</style>

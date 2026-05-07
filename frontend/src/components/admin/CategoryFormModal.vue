<script setup>
import { uiState } from '../../composables/state.js'
import { useAdmin } from '../../composables/useAdmin.js'

const admin = useAdmin()
</script>

<template>
  <Teleport to="body">
    <transition name="mask-fade">
      <div v-if="uiState.showCategoryForm" class="modal-mask" @click="admin.closeCategoryForm">
        <section class="modal-card form-modal compact-form category-editor-modal" @click.stop>
          <div class="modal-head">
            <span class="section-label">{{ uiState.editingCategoryId ? '编辑分类' : '新增分类' }}</span>
            <button class="close-btn" @click="admin.closeCategoryForm">&times;</button>
          </div>

          <div class="form-grid single">
            <label>
              <span>分类名称</span>
              <input v-model="uiState.categoryForm.label" placeholder="例如：早餐套餐" :class="{ error: uiState.categoryFormErrors.label }" />
              <small v-if="uiState.categoryFormErrors.label" class="field-error">{{ uiState.categoryFormErrors.label }}</small>
            </label>
            <label>
              <span>排序</span>
              <input v-model="uiState.categoryForm.sortOrder" type="number" min="1" :class="{ error: uiState.categoryFormErrors.sortOrder }" />
              <small v-if="uiState.categoryFormErrors.sortOrder" class="field-error">{{ uiState.categoryFormErrors.sortOrder }}</small>
            </label>
          </div>

          <div class="modal-footer">
            <strong>{{ uiState.editingCategoryId ? '更新分类' : '创建分类' }}</strong>
            <div class="modal-actions">
              <button class="ghost-btn" @click="admin.closeCategoryForm">取消</button>
              <button class="primary-btn" :disabled="uiState.actionLoading || !admin.canSaveCategory.value" @click="admin.saveCategory">
                {{ uiState.actionLoading ? '保存中...' : '确认保存' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </transition>
  </Teleport>
</template>

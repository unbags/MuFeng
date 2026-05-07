<script setup>
import { uiState } from '../../composables/state.js'
import { useAdmin } from '../../composables/useAdmin.js'
import ImageUpload from './ImageUpload.vue'

const admin = useAdmin()
</script>

<template>
  <Teleport to="body">
    <transition name="mask-fade">
      <div v-if="uiState.showDishForm" class="modal-mask" @click="admin.closeDishForm">
        <section class="modal-card form-modal dish-editor-modal" @click.stop>
          <div class="modal-head dish-editor-head">
            <div>
              <span class="section-label">{{ uiState.editingDishId ? '编辑商品' : '新增商品' }}</span>
              <h3>{{ uiState.editingDishId ? '更新商品信息' : '创建新商品' }}</h3>
            </div>
            <button class="close-btn" @click="admin.closeDishForm">&times;</button>
          </div>

          <div class="dish-editor-layout">
            <aside class="dish-editor-preview">
              <ImageUpload
                :model-value="uiState.dishForm.imageUrl"
                @upload="admin.handleDishImageUpload"
              />
            </aside>

            <div class="dish-editor-fields">
              <section class="editor-field-group">
                <h4>基础信息</h4>
                <div class="form-grid compact-editor-grid">
                  <label>
                    <span>商品名称</span>
                    <input v-model="uiState.dishForm.name" placeholder="请输入商品名称" :class="{ error: uiState.dishFormErrors.name }" />
                    <small v-if="uiState.dishFormErrors.name" class="field-error">{{ uiState.dishFormErrors.name }}</small>
                  </label>
                  <label>
                    <span>分类</span>
                    <select v-model="uiState.dishForm.categoryId" :class="{ error: uiState.dishFormErrors.categoryId }">
                      <option v-for="cat in admin.manageableCategories.value" :key="cat.id" :value="cat.id">{{ cat.label }}</option>
                    </select>
                    <small v-if="uiState.dishFormErrors.categoryId" class="field-error">{{ uiState.dishFormErrors.categoryId }}</small>
                  </label>
                  <label>
                    <span>价格</span>
                    <input v-model="uiState.dishForm.price" type="number" min="0.01" step="0.1" :class="{ error: uiState.dishFormErrors.price }" />
                    <small v-if="uiState.dishFormErrors.price" class="field-error">{{ uiState.dishFormErrors.price }}</small>
                  </label>
                  <label>
                    <span>默认上架</span>
                    <select v-model="uiState.dishForm.available">
                      <option :value="true">上架</option>
                      <option :value="false">下架</option>
                    </select>
                  </label>
                </div>
              </section>

              <section class="editor-field-group">
                <h4>商品介绍</h4>
                <div class="form-grid compact-editor-grid">
                  <label class="full-row">
                    <span>商品介绍</span>
                    <textarea v-model="uiState.dishForm.description" rows="3" :class="{ error: uiState.dishFormErrors.description }" />
                    <small v-if="uiState.dishFormErrors.description" class="field-error">{{ uiState.dishFormErrors.description }}</small>
                  </label>
                </div>
              </section>
            </div>
          </div>

          <div class="modal-footer">
            <strong>{{ uiState.editingDishId ? '更新商品信息' : '创建新商品' }}</strong>
            <div class="modal-actions">
              <button class="ghost-btn" @click="admin.closeDishForm">取消</button>
              <button class="primary-btn" :disabled="uiState.actionLoading || !admin.canSaveDish.value" @click="admin.saveDish">
                {{ uiState.actionLoading ? '保存中...' : uiState.editingDishId ? '确认修改' : '确认新增' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </transition>
  </Teleport>
</template>

<template>
  <div class="container-fluid p-4">
    <h2 id="resource-management-page-heading" class="display-6 fw-bold mb-4">
      <font-awesome-icon icon="trash-alt" class="me-2" />
      <span>게시글 리소스 관리 (휴지통)</span>
    </h2>

    <div class="card shadow-sm border-0 mb-4">
      <div class="card-body p-4">
        <!-- 필터 / 탭 선택 영역 -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div class="d-flex gap-2">
            <b-form-select v-model="activeTab" style="width: 200px" class="form-select-lg">
              <option value="boards">게시글 (Boards)</option>
              <option value="uploads">파일 (Uploads)</option>
            </b-form-select>
            <b-button variant="outline-secondary" @click="loadItems()" :disabled="loading" class="px-3">
              <font-awesome-icon icon="sync-alt" :spin="loading" />
            </b-button>
          </div>

          <!-- 하드 삭제 버튼 -->
          <b-button
            variant="danger"
            @click="confirmHardDelete"
            :disabled="selectedIds.length === 0 || isHardDeleting"
            class="px-4 fw-bold"
          >
            <font-awesome-icon icon="exclamation-triangle" class="me-2" />
            선택 항목 영구 삭제 ({{ selectedIds.length }})
          </b-button>
        </div>

        <!-- 데이터 테이블 -->
        <div class="table-responsive">
          <table class="table table-hover align-middle border-top">
            <thead class="bg-light bg-opacity-50">
              <tr>
                <th style="width: 50px">
                  <b-form-checkbox :modelValue="isAllSelected" @change="toggleSelectAll($event)"></b-form-checkbox>
                </th>
                <th style="width: 80px">ID</th>
                <th v-if="activeTab === 'boards'">제목</th>
                <th v-if="activeTab === 'uploads'">파일명 / 타입</th>
                <th style="width: 120px">작성자 / 소유자</th>
                <th style="width: 160px">삭제 일시</th>
                <th>삭제 사유 / 설명</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading" class="text-center">
                <td colspan="7" class="py-5">
                  <b-spinner variant="primary" label="Loading..."></b-spinner>
                  <p class="mt-2 text-muted">데이터를 불러오는 중입니다...</p>
                </td>
              </tr>
              <tr v-else-if="items.length === 0" class="text-center">
                <td colspan="7" class="py-5 text-muted">
                  <font-awesome-icon icon="info-circle" class="mb-2 fs-3" />
                  <p>삭제된 항목이 없습니다.</p>
                </td>
              </tr>
              <tr v-else v-for="item in items" :key="item.id">
                <td>
                  <b-form-checkbox v-model="selectedIds" :value="item.id"></b-form-checkbox>
                </td>
                <td>{{ item.id }}</td>
                
                <!-- Boards Column -->
                <td v-if="activeTab === 'boards'">
                  <div class="fw-bold fs-6 text-dark">{{ item.title }}</div>
                  <div class="small text-muted text-truncate" style="max-width: 300px">{{ item.content }}</div>
                </td>

                <!-- Uploads Column -->
                <td v-if="activeTab === 'uploads'">
                  <div class="fw-bold fs-6 text-dark">{{ item.sourceFilename }}</div>
                  <div class="small text-muted">{{ item.mimeType }} ({{ (item.fileSize / 1024).toFixed(1) }} KB)</div>
                </td>

                <td>
                  <span v-if="activeTab === 'boards'">{{ item.userId }}</span>
                  <span v-else>Owner #{{ item.id }}</span> <!-- Upload의 경우 추가 정보 연동 필요 가능 -->
                </td>
                
                <td>
                  <div class="small">{{ item.createdDate ? $d(item.createdDate, 'short') : '날짜 없음' }}</div>
                </td>
                
                <td class="text-danger small">
                  {{ item.description || '사유 없음' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 영구 삭제 확인 모달 -->
    <b-modal
      v-model="showConfirmModal"
      title="영구 삭제 경고"
      header-bg-variant="danger"
      header-text-variant="white"
      @ok.prevent="handleHardDelete"
      :ok-disabled="isHardDeleting"
      ok-title="확인 (영구 삭제)"
      cancel-title="취소"
    >
      <div class="text-center p-3">
        <font-awesome-icon icon="exclamation-triangle" class="text-danger display-4 mb-3" />
        <h4 class="fw-bold">정말로 영구 삭제하시겠습니까?</h4>
        <p class="text-muted">
          선택된 <strong>{{ selectedIds.length }}개</strong>의 항목이 데이터베이스에서 물리적으로 사라집니다.<br />
          <strong>파일인 경우 실제 저장소에서도 삭제되며, 복구가 불가능합니다.</strong>
        </p>
      </div>
      <template #modal-footer="{ ok, cancel }">
        <b-button variant="outline-secondary" @click="cancel()">취소</b-button>
        <b-button variant="danger" @click="ok()" :disabled="isHardDeleting">
          <b-spinner small v-if="isHardDeleting" class="me-1"></b-spinner>
          예, 영구 삭제합니다
        </b-button>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./resource-management.component.ts"></script>

<style scoped>
.table th {
  font-weight: 600;
  color: #4a5568;
}
.display-6 {
  font-size: 1.8rem;
  letter-spacing: -0.5px;
}
</style>

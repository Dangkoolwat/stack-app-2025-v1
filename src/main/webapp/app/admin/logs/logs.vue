<template>
  <div class="table-responsive">
    <h2 id="logs-page-heading" data-cy="logsPageHeading">{{ t$('logs.title') }}</h2>

    <div v-if="loggers">
      <p>{{ t$('logs.nbloggers', { total: loggers.length }) }}</p>

      <div class="logs-controls">
        <span>{{ t$('logs.filter') }}</span>
        <input type="text" v-model="filtered" class="form-control logs-filter-input" />
      </div>

      <table class="table table-sm table-striped table-bordered logs-table" aria-describedby="Logs">
        <thead>
          <tr title="click to order">
            <th @click="changeOrder('name')" scope="col">
              <span>{{ t$('logs.table.name') }}</span>
              <jhi-sort-indicator :current-order="orderProp" :reverse="reverse" :field-name="'name'"></jhi-sort-indicator>
            </th>
            <th @click="changeOrder('level')" scope="col">
              <span>{{ t$('logs.table.level') }}</span>
              <jhi-sort-indicator :current-order="orderProp" :reverse="reverse" :field-name="'level'"></jhi-sort-indicator>
            </th>
          </tr>
        </thead>

        <tr v-for="logger in filteredLoggers" :key="logger.name">
          <td>
            <small>{{ logger.name }}</small>
          </td>
          <td>
            <BButtonGroup role="group" aria-label="Log level" class="flex-nowrap logs-btn-group">
              <BButton @click="updateLevel(logger.name, 'TRACE')" :variant="logger.level === 'TRACE' ? 'primary' : 'light'" size="xs" class="logs-btn" v-b-tooltip.hover :title="'Set TRACE'">
                TRACE
              </BButton>
              <BButton @click="updateLevel(logger.name, 'DEBUG')" :variant="logger.level === 'DEBUG' ? 'success' : 'light'" size="xs" class="logs-btn" v-b-tooltip.hover :title="'Set DEBUG'">
                DEBUG
              </BButton>
              <BButton @click="updateLevel(logger.name, 'INFO')" :variant="logger.level === 'INFO' ? 'info' : 'light'" size="xs" class="logs-btn" v-b-tooltip.hover :title="'Set INFO'">
                INFO
              </BButton>
              <BButton @click="updateLevel(logger.name, 'WARN')" :variant="logger.level === 'WARN' ? 'warning' : 'light'" size="xs" class="logs-btn" v-b-tooltip.hover :title="'Set WARN'">
                WARN
              </BButton>
              <BButton @click="updateLevel(logger.name, 'ERROR')" :variant="logger.level === 'ERROR' ? 'danger' : 'light'" size="xs" class="logs-btn" v-b-tooltip.hover :title="'Set ERROR'">
                ERROR
              </BButton>
              <BButton @click="updateLevel(logger.name, 'OFF')" :variant="logger.level === 'OFF' ? 'secondary' : 'light'" size="xs" class="logs-btn" v-b-tooltip.hover :title="'Set OFF'">
                OFF
              </BButton>
            </BButtonGroup>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>

<script lang="ts" src="./logs.component.ts"></script>

<style scoped>
/* Logs 페이지 테이블 레이아웃 개선 */
.logs-table {
  table-layout: fixed;
  width: 100%;
}

/* 컨트롤 영역 간격 */
.logs-controls {
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* 필터 입력칸 중간 크기 */
.logs-filter-input {
  width: 250px;
  max-width: 50%;
  display: inline-block;
}

/* 첫 번째 컬럼 (이름): 30% 너비, 말줄임표 처리 */
.logs-table th:nth-child(1),
.logs-table td:nth-child(1) {
  width: 30%;
  max-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 두 번째 컬럼 (레벨): 70% 너비 */
.logs-table th:nth-child(2),
.logs-table td:nth-child(2) {
  width: 70%;
}

/* 버튼 그룹 최소화 */
.logs-btn-group {
  display: inline-flex;
  flex-wrap: nowrap;
}

/* 버튼 추가 축소 - 글자 전체 표시 */
.logs-btn {
  font-size: 10px;
  padding: 2px 6px;
  line-height: 1.2;
  white-space: nowrap;
}

/* 호버 시 전체 이름 표시 */
.logs-table td:nth-child(1) {
  position: relative;
}

.logs-table td:nth-child(1):hover small {
  overflow: visible;
  white-space: normal;
  word-break: break-all;
}

.logs-table td:nth-child(1) small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

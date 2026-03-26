<template>
  <div>
    <h2 id="configuration-page-heading" data-cy="configurationPageHeading">{{ t$('configuration.title') }}</h2>

    <div v-if="allConfiguration && configuration">
      <div class="config-controls">
        <span>{{ t$('configuration.filter') }}</span>
        <input type="text" v-model="filtered" class="form-control config-filter-input" />
      </div>
      <h3>Spring configuration</h3>
      <table class="table table-striped table-bordered table-responsive d-table config-table" aria-describedby="Configuration">
        <thead>
          <tr>
            <th class="config-col-prefix" @click="changeOrder('prefix')" scope="col">
              <span>{{ t$('configuration.table.prefix') }}</span>
              <jhi-sort-indicator :current-order="orderProp" :reverse="reverse" :field-name="'prefix'"></jhi-sort-indicator>
            </th>
            <th class="config-col-properties" @click="changeOrder('properties')" scope="col">
              <span>{{ t$('configuration.table.properties') }}</span>
              <jhi-sort-indicator :current-order="orderProp" :reverse="reverse" :field-name="'properties'"></jhi-sort-indicator>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in filteredConfiguration" :key="entry.prefix">
            <td>
              <span>{{ entry.prefix }}</span>
            </td>
            <td>
              <div class="row" v-for="key in keys(entry.properties)" :key="key">
                <div class="col-md-4">{{ key }}</div>
                <div class="col-md-8">
                  <span class="float-end bg-secondary break">{{ entry.properties[key] }}</span>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-for="key in keys(allConfiguration)" :key="key">
        <h4>
          <span>{{ key }}</span>
        </h4>
        <table class="table table-sm table-striped table-bordered table-responsive d-table config-table" aria-describedby="Properties">
          <thead>
            <tr>
              <th class="config-col-prefix" scope="col">Property</th>
              <th class="config-col-properties" scope="col">Value</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item of allConfiguration[key]" :key="item.key">
              <td class="break">{{ item.key }}</td>
              <td class="break">
                <span class="float-end bg-secondary break">{{ item.val }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./configuration.component.ts"></script>

<style scoped>
/* Configuration 페이지 레이아웃 개선 */
.config-controls {
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* 필터 입력칸 중간 크기 */
.config-filter-input {
  width: 250px;
  max-width: 50%;
  display: inline-block;
}

/* 테이블 레이아웃 고정 */
.config-table {
  table-layout: fixed;
  width: 100%;
}

/* 첫 번째 컬럼 (Prefix/Property): 30% 너비 */
.config-col-prefix,
.config-table td:first-child {
  width: 30%;
  max-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 두 번째 컬럼 (Properties/Value): 70% 너비 */
.config-col-properties,
.config-table td:last-child {
  width: 70%;
}

/* 호버 시 전체 텍스트 표시 */
.config-table td:first-child {
  position: relative;
}

.config-table td:first-child:hover {
  overflow: visible;
  white-space: normal;
  word-break: break-all;
}
</style>

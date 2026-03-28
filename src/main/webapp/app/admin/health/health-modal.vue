<template>
  <div class="modal-body pad">
    <div v-if="currentHealth?.details">
      <h5>{{ t$('health.details.properties') }}</h5>
      <div class="table-responsive">
        <table class="table table-striped" aria-describedby="Health">
          <thead>
            <tr>
              <th class="text-start" scope="col">{{ t$('health.details.name') }}</th>
              <th class="text-start" scope="col">{{ t$('health.details.value') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="[detailName, item] in detailEntries()" :key="detailName">
              <td class="text-start">{{ detailName }}</td>
              <td class="text-start">
                <div v-if="isObjectValue(item)" class="table-responsive">
                  <table class="table table-sm table-bordered align-middle mb-0">
                    <tbody>
                      <tr v-for="[childKey, childValue] in entryList(item)" :key="childKey">
                        <td class="w-40 text-muted">{{ childKey }}</td>
                        <td class="text-start">{{ readableValue(childValue) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <span v-else>{{ readableValue(item) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div v-if="currentHealth?.error">
      <h4>{{ t$('health.details.error') }}</h4>
      <pre>{{ currentHealth.error }}</pre>
    </div>
  </div>
</template>

<script lang="ts" src="./health-modal.component.ts"></script>

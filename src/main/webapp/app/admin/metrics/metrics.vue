<template>
  <div>
    <h2>
      <span id="metrics-page-heading" v-text="t$('metrics.title')" data-cy="metricsPageHeading"></span>
      <n-button class="float-end" @click="refresh()">
        <span v-text="t$('metrics[\'refresh.button\']')"></span>
      </n-button>
    </h2>

    <h3 v-text="t$('metrics.jvm.title')"></h3>
    <div class="row" v-if="!updatingMetrics">
      <div class="col-md-4">
        <h4 v-text="t$('metrics.jvm.memory.title')"></h4>
        <div>
          <div v-for="(entry, key) of metrics.jvm" :key="key">
            <span v-if="entry.max !== -1">
              <span>{{ key }}</span> ({{ formatNumber1(entry.used / 1048576) }}M / {{ formatNumber1(entry.max / 1048576) }}M)
            </span>
            <span v-else>
              <span>{{ key }}</span> {{ formatNumber1(entry.used / 1048576) }}M
            </span>
            <div>Committed : {{ formatNumber1(entry.committed / 1048576) }}M</div>
            <n-progress
              v-if="entry.max !== -1"
              type="line"
              :percentage="Math.round((entry.used * 100) / entry.max)"
              :indicator-placement="'inside'"
              :status="'success'"
            >
            </n-progress>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <h4 v-text="t$('metrics.jvm.threads.title')"></h4>
        <span><span v-text="t$('metrics.jvm.threads.runnable')"></span> {{ threadStats.threadDumpRunnable }}</span>
        <n-progress
          type="line"
          :percentage="Math.round((threadStats.threadDumpRunnable * 100) / threadStats.threadDumpAll)"
          :status="'success'"
        >
        </n-progress>

        <span><span v-text="t$('metrics.jvm.threads.timedwaiting')"></span> ({{ threadStats.threadDumpTimedWaiting }})</span>
        <n-progress
          type="line"
          :percentage="Math.round((threadStats.threadDumpTimedWaiting * 100) / threadStats.threadDumpAll)"
          :status="'success'"
        >
        </n-progress>

        <span><span v-text="t$('metrics.jvm.threads.waiting')"></span> ({{ threadStats.threadDumpWaiting }})</span>
        <n-progress
          type="line"
          :percentage="Math.round((threadStats.threadDumpWaiting * 100) / threadStats.threadDumpAll)"
          :status="'success'"
        >
        </n-progress>

        <span><span v-text="t$('metrics.jvm.threads.blocked')"></span> ({{ threadStats.threadDumpBlocked }})</span>
        <n-progress
          type="line"
          :percentage="Math.round((threadStats.threadDumpBlocked * 100) / threadStats.threadDumpAll)"
          :status="'success'"
        >
        </n-progress>

        <span
          >Total: {{ threadStats.threadDumpAll }}
          <a class="hand" @click="showMetricsModal = true">👁</a>
        </span>
      </div>
      <div class="col-md-4">
        <h4>System</h4>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-4">Uptime</div>
          <div class="col-md-8 text-end">{{ convertMillisecondsToDuration(metrics.processMetrics['process.uptime']) }}</div>
        </div>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-4">Start time</div>
          <div class="col-md-8 text-end">{{ formatDate(metrics.processMetrics['process.start.time']) }}</div>
        </div>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-9">Process CPU usage</div>
          <div class="col-md-3 text-end">{{ formatNumber2(100 * metrics.processMetrics['process.cpu.usage']) }} %</div>
        </div>
        <n-progress type="line" :percentage="Math.round(100 * metrics.processMetrics['process.cpu.usage'])" :status="'success'">
        </n-progress>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-9">System CPU usage</div>
          <div class="col-md-3 text-end">{{ formatNumber2(100 * metrics.processMetrics['system.cpu.usage']) }} %</div>
        </div>
        <n-progress type="line" :percentage="Math.round(100 * metrics.processMetrics['system.cpu.usage'])" :status="'success'">
        </n-progress>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-9">System CPU count</div>
          <div class="col-md-3 text-end">{{ metrics.processMetrics['system.cpu.count'] }}</div>
        </div>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-9">System 1m Load average</div>
          <div class="col-md-3 text-end">{{ formatNumber2(metrics.processMetrics['system.load.average.1m']) }}</div>
        </div>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-9">Process files max</div>
          <div class="col-md-3 text-end">{{ formatNumber1(metrics.processMetrics['process.files.max']) }}</div>
        </div>
        <div class="row" v-if="!updatingMetrics">
          <div class="col-md-9">Process files open</div>
          <div class="col-md-3 text-end">{{ formatNumber1(metrics.processMetrics['process.files.open']) }}</div>
        </div>
      </div>
    </div>

    <h3 v-text="t$('metrics.jvm.gc.title')"></h3>
    <div class="row" v-if="!updatingMetrics && isObjectExisting(metrics, 'garbageCollector')">
      <div class="col-md-4">
        <div>
          <span>
            GC Live Data Size/GC Max Data Size ({{ formatNumber1(metrics.garbageCollector['jvm.gc.live.data.size'] / 1048576) }}M /
            {{ formatNumber1(metrics.garbageCollector['jvm.gc.max.data.size'] / 1048576) }}M)
          </span>
          <n-progress
            type="line"
            :percentage="
              Math.round((100 * metrics.garbageCollector['jvm.gc.live.data.size']) / metrics.garbageCollector['jvm.gc.max.data.size'])
            "
            :status="'success'"
          >
          </n-progress>
        </div>
      </div>
      <div class="col-md-4">
        <div>
          <span>
            GC Memory Promoted/GC Memory Allocated ({{ formatNumber1(metrics.garbageCollector['jvm.gc.memory.promoted'] / 1048576) }}M /
            {{ formatNumber1(metrics.garbageCollector['jvm.gc.memory.allocated'] / 1048576) }}M)
          </span>
          <n-progress
            type="line"
            :percentage="
              Math.round((100 * metrics.garbageCollector['jvm.gc.memory.promoted']) / metrics.garbageCollector['jvm.gc.memory.allocated'])
            "
            :status="'success'"
          >
          </n-progress>
        </div>
      </div>
      <div class="col-md-4">
        <div class="row">
          <div class="col-md-9">Classes loaded</div>
          <div class="col-md-3 text-end">{{ metrics.garbageCollector.classesLoaded }}</div>
        </div>
        <div class="row">
          <div class="col-md-9">Classes unloaded</div>
          <div class="col-md-3 text-end">{{ metrics.garbageCollector.classesUnloaded }}</div>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table table-striped" aria-describedby="Jvm gc">
          <thead>
            <tr>
              <th scope="col"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.count')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.mean')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.min')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p50')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p75')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p95')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p99')"></th>
              <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.max')"></th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>jvm.gc.pause</td>
              <td class="text-end">{{ metrics.garbageCollector['jvm.gc.pause'].count }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause'].mean) }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause']['0.0']) }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause']['0.5']) }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause']['0.75']) }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause']['0.95']) }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause']['0.99']) }}</td>
              <td class="text-end">{{ formatNumber2(metrics.garbageCollector['jvm.gc.pause'].max) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <h3 v-text="t$('metrics.jvm.http.title')"></h3>
    <table
      class="table table-striped"
      v-if="!updatingMetrics && isObjectExisting(metrics, 'http.server.requests')"
      aria-describedby="Jvm http"
    >
      <thead>
        <tr>
          <th scope="col" v-text="t$('metrics.jvm.http.table.code')"></th>
          <th scope="col" v-text="t$('metrics.jvm.http.table.count')"></th>
          <th scope="col" class="text-end" v-text="t$('metrics.jvm.http.table.mean')"></th>
          <th scope="col" class="text-end" v-text="t$('metrics.jvm.http.table.max')"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(entry, key) of metrics['http.server.requests']['percode']" :key="key">
          <td>{{ key }}</td>
          <td>
            <n-progress
              type="line"
              :percentage="Math.round((entry.count * 100) / metrics['http.server.requests']['all'].count)"
              :status="'success'"
            >
              {{ formatNumber1(entry.count) }}
            </n-progress>
          </td>
          <td class="text-end">
            {{ formatNumber2(filterNaN(entry.mean)) }}
          </td>
          <td class="text-end">{{ formatNumber2(entry.max) }}</td>
        </tr>
      </tbody>
    </table>

    <h3>Endpoints requests (time in millisecond)</h3>
    <div class="table-responsive" v-if="!updatingMetrics">
      <table class="table table-striped" aria-describedby="Endpoint">
        <thead>
          <tr>
            <th scope="col">Method</th>
            <th scope="col">Endpoint url</th>
            <th scope="col" class="text-end">Count</th>
            <th scope="col" class="text-end">Mean</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(entry, entryKey) of metrics.services">
            <tr v-for="(method, methodKey) of entry" :key="entryKey + '-' + methodKey">
              <td>{{ methodKey }}</td>
              <td>{{ entryKey }}</td>
              <td class="text-end">{{ method.count }}</td>
              <td class="text-end">{{ formatNumber2(method.mean) }}</td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <h3 v-text="t$('metrics.cache.title')"></h3>
    <div class="table-responsive" v-if="!updatingMetrics && isObjectExisting(metrics, 'cache')">
      <table class="table table-striped" aria-describedby="Cache">
        <thead>
          <tr>
            <th scope="col" v-text="t$('metrics.cache.cachename')"></th>
            <th scope="col" class="text-end" data-translate="metrics.cache.hits">Cache Hits</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.misses">Cache Misses</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.gets">Cache Gets</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.puts">Cache Puts</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.removals">Cache Removals</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.evictions">Cache Evictions</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.hitPercent">Cache Hit %</th>
            <th scope="col" class="text-end" data-translate="metrics.cache.missPercent">Cache Miss %</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(entry, key) of metrics.cache" :key="key">
            <td>{{ key }}</td>
            <td class="text-end">{{ entry['cache.gets.hit'] }}</td>
            <td class="text-end">{{ entry['cache.gets.miss'] }}</td>
            <td class="text-end">{{ entry['cache.gets.hit'] + entry['cache.gets.miss'] }}</td>
            <td class="text-end">{{ entry['cache.puts'] }}</td>
            <td class="text-end">{{ entry['cache.removals'] }}</td>
            <td class="text-end">{{ entry['cache.evictions'] }}</td>
            <td class="text-end">
              {{ formatNumber2(filterNaN((100 * entry['cache.gets.hit']) / (entry['cache.gets.hit'] + entry['cache.gets.miss']))) }}
            </td>
            <td class="text-end">
              {{ formatNumber2(filterNaN((100 * entry['cache.gets.miss']) / (entry['cache.gets.hit'] + entry['cache.gets.miss']))) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <h3 v-text="t$('metrics.datasource.title')"></h3>
    <div class="table-responsive" v-if="!updatingMetrics && isObjectExistingAndNotEmpty(metrics, 'databases')">
      <table class="table table-striped" aria-describedby="Connection pool">
        <thead>
          <tr>
            <th scope="col">
              <span v-text="t$('metrics.datasource.usage')"></span> (active: {{ metrics.databases.active.value }}, min:
              {{ metrics.databases.min.value }}, max: {{ metrics.databases.max.value }}, idle: {{ metrics.databases.idle.value }})
            </th>
            <th scope="col" class="text-end" v-text="t$('metrics.datasource.count')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.datasource.mean')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.min')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p50')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p75')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p95')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.servicesstats.table.p99')"></th>
            <th scope="col" class="text-end" v-text="t$('metrics.datasource.max')"></th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Acquire</td>
            <td class="text-end">{{ metrics.databases.acquire.count }}</td>
            <td class="text-end">{{ formatNumber2(filterNaN(metrics.databases.acquire.mean)) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.acquire['0.0']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.acquire['0.5']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.acquire['0.75']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.acquire['0.95']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.acquire['0.99']) }}</td>
            <td class="text-end">{{ formatNumber2(filterNaN(metrics.databases.acquire.max)) }}</td>
          </tr>
          <tr>
            <td>Creation</td>
            <td class="text-end">{{ metrics.databases.creation.count }}</td>
            <td class="text-end">{{ formatNumber2(filterNaN(metrics.databases.creation.mean)) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.creation['0.0']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.creation['0.5']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.creation['0.75']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.creation['0.95']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.creation['0.99']) }}</td>
            <td class="text-end">{{ formatNumber2(filterNaN(metrics.databases.creation.max)) }}</td>
          </tr>
          <tr>
            <td>Usage</td>
            <td class="text-end">{{ metrics.databases.usage.count }}</td>
            <td class="text-end">{{ formatNumber2(filterNaN(metrics.databases.usage.mean)) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.usage['0.0']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.usage['0.5']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.usage['0.75']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.usage['0.95']) }}</td>
            <td class="text-end">{{ formatNumber2(metrics.databases.usage['0.99']) }}</td>
            <td class="text-end">{{ formatNumber2(filterNaN(metrics.databases.usage.max)) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <n-modal v-model:show="showMetricsModal" preset="card" :style="{ width: '900px' }" :title="t$('metrics.jvm.threads.dump.title')">
      <metrics-modal :thread-dump="threadData"></metrics-modal>
    </n-modal>
  </div>
</template>

<script lang="ts" src="./metrics.component.ts"></script>

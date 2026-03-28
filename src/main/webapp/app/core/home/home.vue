<template>
  <div v-if="!isInitialized" class="text-center p-5">
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>

  <div class="dc-page home-page" v-if="isInitialized">
    <template v-if="!authenticated">
      <section class="home-hero dc-panel">
        <div class="home-hero__content">
          <p class="home-hero__eyebrow">Framework Landing</p>
          <h1 class="home-hero__title">{{ t$('home.title') }}</h1>
          <p class="home-hero__body">{{ t$('home.subtitle') }}</p>
          <p class="home-hero__body home-hero__body--subtle">{{ t$('home.intro') }}</p>
          <div class="dc-page-actions mt-4">
            <button class="btn btn-primary btn-lg px-4" @click="showLogin()">
              <font-awesome-icon icon="sign-in-alt" class="me-2" />{{ t$('global.messages.info.authenticated.link') }}
            </button>
            <router-link to="/register" class="btn btn-outline-secondary btn-lg px-4">
              <font-awesome-icon icon="user-plus" class="me-2" />{{ t$('global.messages.info.register.link') }}
            </router-link>
          </div>
        </div>

        <div class="home-hero__highlights">
          <div class="home-highlight">
            <span class="home-highlight__label">API</span>
            <strong class="home-highlight__value">Spring Boot 4</strong>
            <p class="home-highlight__desc">{{ t$('home.goals.consistency.desc') }}</p>
          </div>
          <div class="home-highlight">
            <span class="home-highlight__label">Security</span>
            <strong class="home-highlight__value">JWT + Role</strong>
            <p class="home-highlight__desc">{{ t$('home.goals.security.desc') }}</p>
          </div>
          <div class="home-highlight">
            <span class="home-highlight__label">Frontend</span>
            <strong class="home-highlight__value">Vue 3 + Vite</strong>
            <p class="home-highlight__desc">{{ t$('home.features.vue') }}</p>
          </div>
        </div>
      </section>

      <section class="dc-stat-grid">
        <article class="dc-stat-card">
          <span class="dc-stat-card__label">{{ t$('home.features.title') }}</span>
          <div class="dc-stat-card__value">REST</div>
          <p class="dc-stat-card__hint">{{ t$('home.features.arch') }}</p>
        </article>
        <article class="dc-stat-card">
          <span class="dc-stat-card__label">Database</span>
          <div class="dc-stat-card__value">Liquibase</div>
          <p class="dc-stat-card__hint">{{ t$('home.features.db') }}</p>
        </article>
        <article class="dc-stat-card">
          <span class="dc-stat-card__label">Deploy</span>
          <div class="dc-stat-card__value">Cloud + On-Prem</div>
          <p class="dc-stat-card__hint">{{ t$('home.features.cloud') }}</p>
        </article>
        <article class="dc-stat-card">
          <span class="dc-stat-card__label">UI</span>
          <div class="dc-stat-card__value">Bootstrap</div>
          <p class="dc-stat-card__hint">{{ t$('home.tech.frontend.stack') }}</p>
        </article>
      </section>

      <section class="home-dashboard-grid">
        <div class="dc-panel">
          <div class="dc-panel__body">
            <div class="home-section-head">
              <div>
                <h2 class="home-section-head__title">{{ t$('home.goals.title') }}</h2>
                <p class="home-section-head__desc">프레임워크가 기본으로 제공하는 운영 중심 설계 포인트입니다.</p>
              </div>
            </div>
            <div class="dc-info-list">
              <div class="dc-info-list__item" v-for="goal in ['consistency', 'security', 'performance', 'scalability', 'docs']" :key="goal">
                <div class="dc-info-list__label">{{ t$(`home.goals.${goal}.title`) }}</div>
                <div class="dc-info-list__value">{{ t$(`home.goals.${goal}.desc`) }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="dc-panel">
          <div class="dc-panel__body">
            <div class="home-section-head">
              <div>
                <h2 class="home-section-head__title">{{ t$('home.tech.title') }}</h2>
                <p class="home-section-head__desc">백엔드와 프런트엔드 주요 스펙을 함께 정리했습니다.</p>
              </div>
            </div>
            <div class="dc-info-list">
              <div class="dc-info-list__item">
                <div class="dc-info-list__label">{{ t$('home.tech.backend.title') }}</div>
                <div class="dc-info-list__value">{{ t$('home.tech.backend.stack') }}</div>
              </div>
              <div class="dc-info-list__item">
                <div class="dc-info-list__label">{{ t$('home.tech.frontend.title') }}</div>
                <div class="dc-info-list__value">{{ t$('home.tech.frontend.stack') }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="dc-page-header">
        <div>
          <p class="dc-chip mb-3">Operations Workspace</p>
          <h1 class="dc-page-header__title">{{ username }} 님 작업 대시보드</h1>
          <p class="dc-page-header__subtitle">상태 확인과 운영 메타데이터를 한 곳에서 빠르게 훑어볼 수 있도록 정리했습니다.</p>
        </div>
        <div class="dc-page-actions">
          <router-link v-if="hasAnyAuthority('ROLE_ADMIN')" to="/admin/health" class="btn btn-outline-secondary">
            <font-awesome-icon icon="heart" class="me-2" />{{ t$('global.menu.admin.health') }}
          </router-link>
          <router-link to="/board/new" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt" class="me-2" />{{ t$('home.dashboard.actions.create') }}
          </router-link>
        </div>
      </section>

      <section class="dc-panel" v-if="hasAnyAuthority('ROLE_ADMIN')">
        <div class="dc-panel__body">
          <div class="home-section-head">
            <div>
              <h2 class="home-section-head__title">상태 카드</h2>
              <p class="home-section-head__desc">Database, Redis, Disk Space와 빌드 정보를 우선 카드로 보여 줍니다.</p>
            </div>
          </div>
          <div v-if="adminSummaryLoading" class="dc-empty-state">
            <div class="dc-empty-state__title">관리자 요약 정보를 불러오는 중입니다.</div>
          </div>
          <div v-else class="dc-stat-grid">
            <article
              class="dc-stat-card"
              v-for="card in adminHealthCards"
              :key="card.key"
              :class="{
                'home-admin-card--success': card.tone === 'success',
                'home-admin-card--warning': card.tone === 'warning',
                'home-admin-card--danger': card.tone === 'danger',
              }"
            >
              <span class="dc-stat-card__label">{{ card.label }}</span>
              <div class="dc-stat-card__value">{{ card.value }}</div>
              <p class="dc-stat-card__hint">{{ card.hint }}</p>
            </article>
            <article class="dc-stat-card" v-for="card in adminInfoCards" :key="card.key">
              <span class="dc-stat-card__label">{{ card.label }}</span>
              <div class="dc-stat-card__value home-admin-card__value--small">{{ card.value }}</div>
              <p class="dc-stat-card__hint">{{ card.hint }}</p>
            </article>
          </div>
        </div>
      </section>

      <section class="home-dashboard-grid">
        <div class="dc-panel">
          <div class="dc-panel__body">
            <div class="home-section-head">
              <div>
                <h2 class="home-section-head__title">빠른 진입</h2>
                <p class="home-section-head__desc">가장 자주 쓰는 관리자 및 콘텐츠 화면을 묶어 두었습니다.</p>
              </div>
            </div>
            <div class="dc-link-grid">
              <router-link v-for="link in quickLinks" :key="link.key" :to="link.to" class="dc-link-card">
                <div class="dc-link-card__title">
                  <font-awesome-icon :icon="link.icon" />
                  <span>{{ t$(link.title) }}</span>
                </div>
                <p class="dc-link-card__desc">{{ t$(link.description) }}</p>
              </router-link>
            </div>
          </div>
        </div>

        <div class="dc-panel">
          <div class="dc-panel__body">
            <div class="home-section-head">
              <div>
                <h2 class="home-section-head__title">운영 메모</h2>
                <p class="home-section-head__desc">이번 UI 개선에서 계속 유지할 기준을 정리했습니다.</p>
              </div>
            </div>
            <div class="dc-info-list">
              <div class="dc-info-list__item" v-for="item in attentionItems" :key="item.key">
                <div class="dc-info-list__label">{{ t$(item.label) }}</div>
                <div class="dc-info-list__value">{{ t$(item.value) }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script lang="ts" src="./home.component.ts"></script>

<style scoped>
.home-page {
  gap: 24px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.95fr);
  overflow: hidden;
  background: radial-gradient(circle at top right, rgba(37, 99, 235, 0.16), transparent 32%), linear-gradient(135deg, #ffffff, #f8fbff);
}

.home-hero__content,
.home-hero__highlights {
  padding: 32px;
}

.home-hero__eyebrow {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.home-hero__title {
  max-width: 10ch;
  margin: 0;
  color: #0f172a;
  font-size: clamp(2.4rem, 4vw, 3.6rem);
  line-height: 1;
  letter-spacing: -0.05em;
}

.home-hero__body {
  max-width: 56ch;
  margin: 18px 0 0;
  color: #334155;
  font-size: 1rem;
}

.home-hero__body--subtle {
  color: #64748b;
}

.home-highlight + .home-highlight {
  margin-top: 20px;
}

.home-highlight__label {
  display: block;
  color: #2563eb;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.home-highlight__value {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 1.35rem;
  letter-spacing: -0.03em;
}

.home-highlight__desc {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 0.92rem;
}

.home-section-head {
  margin-bottom: 18px;
}

.home-section-head__title {
  margin: 0;
  color: #111827;
  font-size: 1.15rem;
  font-weight: 700;
}

.home-section-head__desc {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 0.92rem;
}

.home-dashboard-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 16px;
}

.home-admin-card--success {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.home-admin-card--warning {
  border-color: #fde68a;
  background: #fffbeb;
}

.home-admin-card--danger {
  border-color: #fecaca;
  background: #fef2f2;
}

.home-admin-card__value--small {
  font-size: 1.45rem;
}

@media (max-width: 991px) {
  .home-hero,
  .home-dashboard-grid {
    grid-template-columns: 1fr;
  }

  .home-hero__content,
  .home-hero__highlights {
    padding: 24px;
  }
}
</style>

<template>
  <div class="dc-page dc-page--centered">
    <section class="dc-panel dc-panel--error shadow-lg">
      <div class="dc-panel__body text-center p-5">
        <!-- 404 Not Found -->
        <div v-if="error404" class="dc-error-content animate__animated animate__fadeIn">
          <div class="dc-error-illustration mb-4">
            <img src="content/images/404-illustration.png" alt="404 Not Found" class="img-fluid rounded-3" style="max-height: 320px" />
          </div>
          <h1 class="dc-error-title d-block mb-3 fw-bold" v-text="t$('error.http.404.title')"></h1>
          <p class="dc-error-subtitle text-muted mb-5 px-md-5" v-text="t$('error.http.404.subtitle')"></p>
          <div class="dc-error-actions">
            <router-link to="/" class="btn btn-primary btn-lg px-5 dc-btn-premium">
              <font-awesome-icon icon="home" class="me-2"></font-awesome-icon>
              {{ t$('error.http.404.backToHome') }}
            </router-link>
          </div>
        </div>

        <!-- 403 Forbidden -->
        <div v-else-if="error403" class="dc-error-content animate__animated animate__fadeIn">
          <div class="dc-error-illustration mb-4">
            <img src="content/images/403-illustration.png" alt="403 Forbidden" class="img-fluid rounded-3" style="max-height: 320px" />
          </div>
          <h1 class="dc-error-title d-block mb-3 fw-bold text-danger" v-text="t$('error.http.403.title')"></h1>
          <p class="dc-error-subtitle text-muted mb-5 px-md-5" v-text="t$('error.http.403.subtitle')"></p>
          <div class="dc-error-actions">
            <router-link to="/" class="btn btn-outline-danger btn-lg px-5 dc-btn-premium">
              <font-awesome-icon icon="home" class="me-2"></font-awesome-icon>
              {{ t$('error.http.403.backToHome') }}
            </router-link>
          </div>
        </div>

        <!-- Generic Error -->
        <div v-else class="dc-error-content animate__animated animate__fadeIn">
          <div class="dc-error-icon mb-4">
            <font-awesome-icon icon="exclamation-triangle" class="text-warning display-1"></font-awesome-icon>
          </div>
          <h1 class="dc-error-title d-block mb-3 fw-bold" v-text="t$('error.title')"></h1>
          <div v-if="errorMessage" class="alert alert-danger d-inline-block px-4 py-2 rounded-pill mb-4">
            {{ errorMessage }}
          </div>
          <p class="dc-error-subtitle text-muted mb-5" v-text="t$('error.http.500')"></p>
          <div class="dc-error-actions">
            <router-link to="/" class="btn btn-outline-secondary btn-lg px-5 dc-btn-premium">
              <font-awesome-icon icon="home" class="me-2"></font-awesome-icon>
              {{ t$('error.http.404.backToHome') }}
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script lang="ts" src="./error.component.ts"></script>

<style scoped lang="scss">
.dc-page--centered {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  padding: 2rem;
}

.dc-panel--error {
  max-width: 800px;
  width: 100%;
  border: none;
  border-radius: 1.5rem;
  background: white;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px);
  }
}

.dc-error-title {
  font-size: 2.5rem;
  color: #1a202c;
  letter-spacing: -0.025em;
}

.dc-error-subtitle {
  font-size: 1.1rem;
  line-height: 1.6;
}

.dc-btn-premium {
  border-radius: 0.75rem;
  font-weight: 600;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);

  &:hover {
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
    transform: scale(1.02);
  }
}

.dc-error-illustration img {
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.05));
}
</style>

<template>
  <div class="home container-fluid px-4 py-5">
    <!-- Public / Logged-out View -->
    <div v-if="!authenticated" class="public-intro">
      <div class="hero-section text-center mb-5 p-5 bg-light rounded-3 shadow-sm">
        <h1 class="display-4 fw-bold text-primary mb-3"><font-awesome-icon icon="rocket" class="me-2" />{{ t$('home.title') }}</h1>
        <p class="lead fs-4 text-secondary mb-4">{{ t$('home.subtitle') }}</p>
        <p class="mx-auto col-md-8 text-muted">{{ t$('home.intro') }}</p>
        <div class="mt-4">
          <button class="btn btn-primary btn-lg px-4 me-md-2" @click="showLogin()">
            <font-awesome-icon icon="sign-in-alt" class="me-2" />{{ t$('global.messages.info.authenticated.link') }}
          </button>
          <router-link to="/register" class="btn btn-outline-secondary btn-lg px-4">
            <font-awesome-icon icon="user-plus" class="me-2" />{{ t$('global.messages.info.register.link') }}
          </router-link>
        </div>
      </div>

      <div class="goals-section mb-5">
        <h2 class="text-center mb-4 pb-2 border-bottom fw-semibold">
          <font-awesome-icon icon="bullseye" class="me-2 text-danger" />{{ t$('home.goals.title') }}
        </h2>
        <div class="row g-4 justify-content-center text-center">
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm p-3">
              <div class="card-body">
                <div
                  class="feature-icon bg-primary bg-gradient text-white mb-3 mx-auto rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 60px; height: 60px"
                >
                  <font-awesome-icon icon="code" size="lg" />
                </div>
                <h5 class="card-title fw-bold">{{ t$('home.goals.consistency.title') }}</h5>
                <p class="card-text text-muted small">{{ t$('home.goals.consistency.desc') }}</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm p-3">
              <div class="card-body">
                <div
                  class="feature-icon bg-success bg-gradient text-white mb-3 mx-auto rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 60px; height: 60px"
                >
                  <font-awesome-icon icon="shield-alt" size="lg" />
                </div>
                <h5 class="card-title fw-bold">{{ t$('home.goals.security.title') }}</h5>
                <p class="card-text text-muted small">{{ t$('home.goals.security.desc') }}</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm p-3">
              <div class="card-body">
                <div
                  class="feature-icon bg-warning bg-gradient text-white mb-3 mx-auto rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 60px; height: 60px"
                >
                  <font-awesome-icon icon="bolt" size="lg" />
                </div>
                <h5 class="card-title fw-bold">{{ t$('home.goals.performance.title') }}</h5>
                <p class="card-text text-muted small">{{ t$('home.goals.performance.desc') }}</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm p-3">
              <div class="card-body">
                <div
                  class="feature-icon bg-info bg-gradient text-white mb-3 mx-auto rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 60px; height: 60px"
                >
                  <font-awesome-icon icon="expand-arrows-alt" size="lg" />
                </div>
                <h5 class="card-title fw-bold">{{ t$('home.goals.scalability.title') }}</h5>
                <p class="card-text text-muted small">{{ t$('home.goals.scalability.desc') }}</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm p-3">
              <div class="card-body">
                <div
                  class="feature-icon bg-dark bg-gradient text-white mb-3 mx-auto rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 60px; height: 60px"
                >
                  <font-awesome-icon icon="book" size="lg" />
                </div>
                <h5 class="card-title fw-bold">{{ t$('home.goals.docs.title') }}</h5>
                <p class="card-text text-muted small">{{ t$('home.goals.docs.desc') }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tech-stack-section mb-5">
        <h2 class="text-center mb-4 pb-2 border-bottom fw-semibold">
          <font-awesome-icon icon="cogs" class="me-2 text-primary" />{{ t$('home.tech.title') }}
        </h2>
        <div class="row g-4">
          <div class="col-md-6">
            <div class="card border-0 shadow-sm overflow-hidden h-100">
              <div class="card-header bg-primary text-white py-3 fw-bold">
                <font-awesome-icon icon="server" class="me-2" />{{ t$('home.tech.backend.title') }}
              </div>
              <div class="card-body">
                <p class="card-text text-secondary">{{ t$('home.tech.backend.stack') }}</p>
                <div class="list-group list-group-flush list-group-sm">
                  <div class="list-group-item px-0 border-0 small text-muted">
                    <font-awesome-icon icon="check" class="text-success me-2" />{{ t$('home.features.arch') }}
                  </div>
                  <div class="list-group-item px-0 border-0 small text-muted">
                    <font-awesome-icon icon="check" class="text-success me-2" />{{ t$('home.features.db') }}
                  </div>
                  <div class="list-group-item px-0 border-0 small text-muted">
                    <font-awesome-icon icon="check" class="text-success me-2" />{{ t$('home.features.cloud') }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card border-0 shadow-sm overflow-hidden h-100">
              <div class="card-header bg-info text-white py-3 fw-bold">
                <font-awesome-icon icon="desktop" class="me-2" />{{ t$('home.tech.frontend.title') }}
              </div>
              <div class="card-body">
                <p class="card-text text-secondary">{{ t$('home.tech.frontend.stack') }}</p>
                <div class="list-group list-group-flush list-group-sm">
                  <div class="list-group-item px-0 border-0 small text-muted">
                    <font-awesome-icon icon="check" class="text-success me-2" />Vue 3 (Composition API)
                  </div>
                  <div class="list-group-item px-0 border-0 small text-muted">
                    <font-awesome-icon icon="check" class="text-success me-2" />{{ t$('home.features.vue') }}
                  </div>
                  <div class="list-group-item px-0 border-0 small text-muted">
                    <font-awesome-icon icon="check" class="text-success me-2" />Standardized UI Components
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Admin / Logged-in View -->
    <div v-if="authenticated" class="admin-dashboard">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="alert alert-success d-flex align-items-center mb-4 shadow-sm" role="alert">
            <font-awesome-icon icon="user" size="2x" class="me-3" />
            <div>
              <h4 class="alert-heading mb-1 fw-bold">{{ t$('home.logged.message', { username }) }}</h4>
              <p class="mb-0 text-muted opacity-75">{{ t$('home.logged.guide') }}</p>
            </div>
          </div>

          <div class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-white border-bottom-0 pt-4 px-4 pb-0">
              <h5 class="fw-bold"><font-awesome-icon icon="tachometer-alt" class="me-2 text-primary" />{{ t$('home.logged.info') }}</h5>
            </div>
            <div class="card-body p-4">
              <table class="table table-borderless mb-0">
                <tbody>
                  <tr>
                    <td class="text-muted ps-0 py-1" style="width: 150px">Login ID</td>
                    <td class="fw-semibold py-1">{{ username }}</td>
                  </tr>
                  <tr>
                    <td class="text-muted ps-0 py-1">Role</td>
                    <td class="py-1"><span class="badge bg-secondary">ROLE_ADMIN</span></td>
                  </tr>
                  <tr>
                    <td class="text-muted ps-0 py-1">Environment</td>
                    <td class="py-1"><span class="badge bg-info text-white">Development</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./home.component.ts"></script>

<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="stackApp.settings.home.createOrEditLabel"
          data-cy="SettingsCreateUpdateHeading"
          v-text="t$('stackApp.settings.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="settings.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="settings.id" readonly />
          </div>

          <div class="form-group mb-3">
            <label
              class="form-control-label"
              v-text="t$('stackApp.settings.tokenValiditySeconds')"
              for="settings-tokenValiditySeconds"
            ></label>
            <input
              type="number"
              class="form-control"
              name="tokenValiditySeconds"
              id="settings-tokenValiditySeconds"
              data-cy="tokenValiditySeconds"
              v-model.number="settings.tokenValiditySeconds"
              required
            />
          </div>

          <div class="form-group mb-3">
            <label
              class="form-control-label"
              v-text="t$('stackApp.settings.tokenValiditySecondsForRememberMe')"
              for="settings-tokenValiditySecondsForRememberMe"
            ></label>
            <input
              type="number"
              class="form-control"
              name="tokenValiditySecondsForRememberMe"
              id="settings-tokenValiditySecondsForRememberMe"
              data-cy="tokenValiditySecondsForRememberMe"
              v-model.number="settings.tokenValiditySecondsForRememberMe"
              required
            />
          </div>

          <div class="form-group mb-3">
            <label
              class="form-control-label"
              v-text="t$('stackApp.settings.loginMaxFailureAttempts')"
              for="settings-loginMaxFailureAttempts"
            ></label>
            <input
              type="number"
              class="form-control"
              name="loginMaxFailureAttempts"
              id="settings-loginMaxFailureAttempts"
              data-cy="loginMaxFailureAttempts"
              v-model.number="settings.loginMaxFailureAttempts"
              required
            />
          </div>

          <div class="form-group mb-3">
            <label class="form-control-label" v-text="t$('stackApp.settings.description')" for="settings-description"></label>
            <input
              type="text"
              class="form-control"
              name="description"
              id="settings-description"
              data-cy="description"
              v-model="settings.description"
            />
          </div>

          <!-- 파일 업로드 정책 관리 섹션 -->
          <hr class="my-5" />
          <h3 class="mb-4">
            <font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;
            <span v-text="t$('stackApp.settings.fileUpload.title')"></span>
          </h3>

          <!-- 1. 전역 기본값 설정 카드 -->
          <div class="card mb-4 border-0 shadow-sm shadow-hover">
            <div class="card-header bg-light py-3">
              <h5 class="mb-0 text-primary font-weight-bold" v-text="t$('stackApp.settings.fileUpload.defaults.title')"></h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-4 form-group mb-3">
                  <label
                    class="form-control-label font-sm text-muted mb-1"
                    v-text="t$('stackApp.settings.fileUpload.defaults.defaultMaxFileSizeBytes')"
                  ></label>
                  <div class="input-group">
                    <input
                      type="number"
                      class="form-control"
                      :value="getMB(settings.fileUploadDefaults.defaultMaxFileSizeBytes)"
                      @input="updateDefaultMB('defaultMaxFileSizeBytes', $event)"
                    />
                    <span class="input-group-text bg-white">MB</span>
                  </div>
                </div>
                <div class="col-md-4 form-group mb-3">
                  <label
                    class="form-control-label font-sm text-muted mb-1"
                    v-text="t$('stackApp.settings.fileUpload.defaults.defaultMaxRequestSizeBytes')"
                  ></label>
                  <div class="input-group">
                    <input
                      type="number"
                      class="form-control"
                      :value="getMB(settings.fileUploadDefaults.defaultMaxRequestSizeBytes)"
                      @input="updateDefaultMB('defaultMaxRequestSizeBytes', $event)"
                    />
                    <span class="input-group-text bg-white">MB</span>
                  </div>
                </div>
                <div class="col-md-4 form-group mb-3 d-flex align-items-end pb-2">
                  <div class="form-check form-switch ps-5">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      id="blockUnmatched"
                      v-model="settings.fileUploadDefaults.blockUnmatched"
                    />
                    <label
                      class="form-check-label ms-2"
                      for="blockUnmatched"
                      v-text="t$('stackApp.settings.fileUpload.defaults.blockUnmatched')"
                    ></label>
                  </div>
                </div>
              </div>
              <div class="form-group mb-0 mt-2">
                <label
                  class="form-control-label font-sm text-muted mb-1"
                  v-text="t$('stackApp.settings.fileUpload.defaults.welcomeMessage')"
                ></label>
                <textarea
                  class="form-control border-light"
                  rows="2"
                  v-model="settings.fileUploadDefaults.welcomeMessage"
                  placeholder="업로드 화면에 표시될 안내 문구를 입력하세요."
                ></textarea>
                <small class="text-info mt-1 d-block"
                  ><i class="bi bi-info-circle me-1"></i>
                  <span v-text="t$('stackApp.settings.fileUpload.defaults.blockUnmatchedHelp')"></span
                ></small>
              </div>
            </div>
          </div>

          <!-- 2. 타입별 상세 정책 목록 -->
          <div class="card mb-4 border-0 shadow-sm">
            <div class="card-header bg-light py-3 d-flex justify-content-between align-items-center">
              <h5 class="mb-0 text-primary font-weight-bold" v-text="t$('stackApp.settings.fileUpload.policy.title')"></h5>
              <div class="d-flex align-items-center">
                <b-dropdown size="sm" variant="outline-primary" class="me-2 rounded-pill" right>
                  <template #button-content>
                    <font-awesome-icon icon="wand-magic-sparkles"></font-awesome-icon>&nbsp;
                    <span v-text="t$('stackApp.settings.fileUpload.policy.template')"></span>
                  </template>
                  <h6 class="dropdown-header text-uppercase font-xs text-muted fw-bold mb-1">DB 추천 템플릿 추가</h6>
                  <b-dropdown-item v-for="tpl in settings.fileTypeTemplates" :key="'qa-' + tpl.key" @click.prevent="applyTemplate(-1, tpl)">
                    <span v-text="tpl.label"></span>
                  </b-dropdown-item>
                  <b-dropdown-divider v-if="settings.fileTypeTemplates && settings.fileTypeTemplates.length > 0"></b-dropdown-divider>
                  <b-dropdown-item v-if="!settings.fileTypeTemplates || settings.fileTypeTemplates.length === 0" disabled>
                    등록된 템플릿이 없습니다.
                  </b-dropdown-item>
                </b-dropdown>
                <button type="button" class="btn btn-primary btn-sm rounded-pill px-3" @click="addPolicy()">
                  <font-awesome-icon icon="plus"></font-awesome-icon>&nbsp;
                  <span v-text="t$('stackApp.settings.fileUpload.policy.add')"></span>
                </button>
              </div>
            </div>
            <div class="card-body p-0">
              <div class="table-responsive">
                <table class="table table-hover mb-0 align-middle">
                  <thead class="table-light">
                    <tr>
                      <th style="width: 15%" class="ps-4" v-text="t$('stackApp.settings.fileUpload.policy.label')"></th>
                      <th style="width: 20%" v-text="t$('stackApp.settings.fileUpload.policy.allowedExtensions')"></th>
                      <th style="width: 25%" v-text="t$('stackApp.settings.fileUpload.policy.allowedMimeTypes')"></th>
                      <th style="width: 15%" v-text="t$('stackApp.settings.fileUpload.policy.maxFileSizeBytes')"></th>
                      <th style="width: 10%" class="text-center" v-text="t$('stackApp.settings.fileUpload.policy.enabled')"></th>
                      <th style="width: 15%" class="pe-4 text-end" v-text="t$('stackApp.settings.fileUpload.policy.actions')"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(policy, index) in settings.fileTypePolicies" :key="index" class="border-bottom">
                      <td class="ps-4">
                        <input
                          type="text"
                          class="form-control form-control-sm mb-1 fw-bold"
                          v-model="policy.label"
                          :placeholder="t$('stackApp.settings.fileUpload.policy.label')"
                        />
                        <input
                          type="text"
                          class="form-control form-control-sm text-secondary bg-light border-0"
                          v-model="policy.key"
                          :placeholder="t$('stackApp.settings.fileUpload.policy.key')"
                          style="font-size: 0.7rem"
                        />
                      </td>
                      <td>
                        <textarea
                          class="form-control form-control-sm"
                          rows="1"
                          :value="getCommaSeparated(policy.allowedExtensions)"
                          @input="updateCommaSeparated(policy, 'allowedExtensions', $event)"
                          placeholder="jpg, png..."
                        ></textarea>
                      </td>
                      <td>
                        <textarea
                          class="form-control form-control-sm"
                          rows="1"
                          :value="getCommaSeparated(policy.allowedMimeTypes)"
                          @input="updateCommaSeparated(policy, 'allowedMimeTypes', $event)"
                          placeholder="image/jpeg, image/png..."
                        ></textarea>
                      </td>
                      <td>
                        <div class="input-group input-group-sm">
                          <input
                            type="number"
                            class="form-control form-control-sm"
                            :value="getMB(policy.maxFileSizeBytes)"
                            @input="updatePolicyMB(policy, $event)"
                          />
                          <span class="input-group-text bg-white">MB</span>
                        </div>
                      </td>
                      <td class="text-center">
                        <div class="form-check form-switch d-inline-block">
                          <input class="form-check-input" type="checkbox" v-model="policy.enabled" />
                        </div>
                      </td>
                      <td class="pe-4 text-end">
                        <div class="d-flex align-items-center">
                          <b-dropdown size="sm" variant="light" class="shadow-sm border rounded me-1" right no-caret>
                            <template #button-content>
                              <font-awesome-icon icon="wand-magic-sparkles" class="text-info"></font-awesome-icon>
                            </template>
                            <h6
                              class="dropdown-header text-uppercase font-xs text-muted fw-bold mb-1"
                              v-text="t$('stackApp.settings.fileUpload.policy.template')"
                            ></h6>
                            <b-dropdown-item
                              v-for="tpl in settings.fileTypeTemplates"
                              :key="'row-tpl-' + tpl.key"
                              @click.prevent="applyTemplate(index, tpl)"
                            >
                              <span v-text="tpl.label"></span>
                            </b-dropdown-item>
                            <b-dropdown-item v-if="!settings.fileTypeTemplates || settings.fileTypeTemplates.length === 0" disabled>
                              등록된 템플릿 없음
                            </b-dropdown-item>
                          </b-dropdown>
                          <button type="button" class="btn btn-sm btn-light shadow-sm border rounded" @click="removePolicy(index)">
                            <font-awesome-icon icon="trash-can" class="text-danger"></font-awesome-icon>
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr v-if="settings.fileTypePolicies.length === 0">
                      <td colspan="6" class="text-center py-5 text-muted bg-light-subtle">
                        <font-awesome-icon icon="folder-open" size="2x" class="mb-3 d-block mx-auto opacity-25"></font-awesome-icon>
                        <span class="fw-light">등록된 파일 정책이 없습니다. 새 정책을 추가해 주세요.</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
        <!-- 파일 정책 템플릿 관리 (DB 기반 동적 관리) -->
        <div class="card border-0 shadow-sm rounded-4 mb-4">
          <div class="card-header bg-white border-bottom-0 pt-4 px-4 pb-0">
            <h5 class="card-title fw-bold mb-1 text-dark text-uppercase font-sm tracking-wider">
              <font-awesome-icon icon="layer-group" class="text-primary me-2"></font-awesome-icon>
              파일 정책 추천 템플릿 관리
            </h5>
            <p class="text-muted mb-2 font-sm">자주 사용하는 파일 정책을 템플릿으로 등록하여 빠르게 추가할 수 있습니다.</p>
            <div class="d-flex gap-2 mb-3">
              <button type="button" class="btn btn-outline-info btn-sm rounded-pill px-3 shadow-sm" @click="toggleGuide()">
                <font-awesome-icon icon="circle-info"></font-awesome-icon>&nbsp; 입력 가이드
              </button>
              <button type="button" class="btn btn-outline-warning btn-sm rounded-pill px-3 shadow-sm" @click="restoreTemplates()">
                <font-awesome-icon icon="rotate-left"></font-awesome-icon>&nbsp; 기본값 복원
              </button>
              <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3 shadow-sm" @click="addTemplate()">
                <font-awesome-icon icon="plus"></font-awesome-icon>&nbsp; 템플릿 추가
              </button>
            </div>
          </div>
          <div class="card-body p-4">
            <!-- 입력 가이드 팝업 (단순 레이어) -->
            <div v-if="showGuide" class="alert alert-info border-info shadow-sm rounded-3 mb-4 animate__animated animate__fadeIn">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <h6 class="fw-bold"><font-awesome-icon icon="lightbulb" class="me-2"></font-awesome-icon>파일 타입 입력 가이드</h6>
                  <p class="font-sm mb-2">자주 사용하는 확장자와 MIME 타입 예시입니다. 복사하여 템플릿에 활용하세요.</p>
                  <div class="table-responsive">
                    <table class="table table-sm table-borderless font-sm mb-0">
                      <thead>
                        <tr class="text-secondary border-bottom">
                          <th>유형</th>
                          <th>확장자 (쉼표로 구분)</th>
                          <th>MIME 타입 (쉼표로 구분)</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="guide in getTemplateGuide()" :key="guide.key">
                          <td class="fw-bold">{{ guide.label }}</td>
                          <td>
                            <code>{{ getCommaSeparated(guide.allowedExtensions) }}</code>
                          </td>
                          <td>
                            <code class="text-break">{{ getCommaSeparated(guide.allowedMimeTypes) }}</code>
                          </td>
                        </tr>
                        <tr>
                          <td class="fw-bold text-primary">포토샵(PSD)</td>
                          <td><code>psd</code></td>
                          <td><code>image/vnd.adobe.photoshop, image/x-photoshop, application/x-photoshop</code></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
                <button type="button" class="btn-close" @click="showGuide = false"></button>
              </div>
            </div>

            <div v-if="settings.fileTypeTemplates && settings.fileTypeTemplates.length > 0" class="table-responsive rounded-3 border">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="ps-4" style="width: 150px">템플릿명</th>
                    <th style="width: 120px">확장자</th>
                    <th>MIME 타입</th>
                    <th style="width: 120px">최대 용량</th>
                    <th class="text-center" style="width: 80px">액션</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(tpl, index) in settings.fileTypeTemplates" :key="'tpl-' + index">
                    <td class="ps-4">
                      <input
                        type="text"
                        class="form-control form-control-sm border-0 bg-light rounded"
                        v-model="tpl.label"
                        placeholder="템플릿 이름"
                      />
                    </td>
                    <td>
                      <input
                        type="text"
                        class="form-control form-control-sm border-0 bg-light rounded"
                        :value="getCommaSeparated(tpl.allowedExtensions)"
                        @input="updateCommaSeparated(tpl, 'allowedExtensions', $event)"
                        placeholder="jpg, png..."
                      />
                    </td>
                    <td>
                      <textarea
                        class="form-control form-control-sm border-0 bg-light rounded"
                        rows="1"
                        :value="getCommaSeparated(tpl.allowedMimeTypes)"
                        @input="updateCommaSeparated(tpl, 'allowedMimeTypes', $event)"
                        placeholder="image/jpeg, image/png..."
                      ></textarea>
                    </td>
                    <td>
                      <div class="input-group input-group-sm">
                        <input
                          type="number"
                          class="form-control border-0 bg-light"
                          :value="getMB(tpl.maxFileSizeBytes)"
                          @input="updatePolicyMB(tpl, $event)"
                        />
                        <span class="input-group-text border-0 bg-light">MB</span>
                      </div>
                    </td>
                    <td class="text-center">
                      <button type="button" class="btn btn-link btn-sm text-danger p-0" @click="removeTemplate(index)">
                        <font-awesome-icon icon="trash-can"></font-awesome-icon>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-center py-5 bg-light rounded-3 border border-dashed">
              <font-awesome-icon icon="layer-group" class="fa-2x text-muted mb-2 opacity-25"></font-awesome-icon>
              <p class="text-muted mb-0 font-sm">등록된 추천 템플릿이 없습니다. 새로운 템플릿을 추가해 보세요.</p>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mb-5 px-2">
          <button type="button" class="btn btn-light rounded-pill px-4 py-2 border shadow-sm" @click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.cancel')"></span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="isSaving"
            class="btn btn-primary rounded-pill px-5 py-2 shadow border-0 gradient-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./settings.component.ts"></script>

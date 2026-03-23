import { defineComponent, inject, ref, type Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import SettingsService from './settings.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'SettingsUpdate',
  setup() {
    const settingsService = inject('settingsService', () => new SettingsService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);
    const { t: t$ } = useI18n();
    const router = useRouter();

    const settings: Ref<any> = ref({
      fileUploadDefaults: {
        defaultMaxFileSizeBytes: 10485760,
        defaultMaxRequestSizeBytes: 20971520,
        blockUnmatched: true,
        welcomeMessage: '',
      },
      fileTypePolicies: [],
      fileTypeTemplates: [],
    });
    const isSaving = ref(false);
    const showGuide = ref(false); // 가이드 팝업 상태
    const fileTypeTemplates = [
      // 기본 복원용 정적 데이터
      {
        key: 'image-standard',
        label: '이미지(표준)',
        allowedExtensions: ['jpg', 'jpeg', 'png', 'gif', 'webp'],
        allowedMimeTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
        maxFileSizeBytes: 10485760,
        description: '표준 이미지 허용 정책',
      },
      {
        key: 'pdf-standard',
        label: 'PDF 문서',
        allowedExtensions: ['pdf'],
        allowedMimeTypes: ['application/pdf'],
        maxFileSizeBytes: 20971520,
        description: '공식 문서(PDF) 허용 정책',
      },
      {
        key: 'office-modern',
        label: 'MS 오피스(신규)',
        allowedExtensions: ['docx', 'xlsx', 'pptx'],
        allowedMimeTypes: [
          'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        ],
        maxFileSizeBytes: 20971520,
        description: '최신 MS 오피스 문서 정책',
      },
      {
        key: 'office-legacy',
        label: 'MS 오피스(구형)',
        allowedExtensions: ['doc', 'xls', 'ppt'],
        allowedMimeTypes: ['application/msword', 'application/vnd.ms-excel', 'application/vnd.ms-powerpoint'],
        maxFileSizeBytes: 20971520,
        description: '구형 MS 오피스 문서 정책',
      },
      {
        key: 'hwp-standard',
        label: '한글 문서(HWP/HWPX)',
        allowedExtensions: ['hwp', 'hwpx'],
        allowedMimeTypes: ['application/x-hwp', 'application/haansofthwpx'],
        maxFileSizeBytes: 20971520,
        description: '한글 문서(HWP) 정책',
      },
      {
        key: 'video-standard',
        label: '비디오(MP4)',
        allowedExtensions: ['mp4', 'webm'],
        allowedMimeTypes: ['video/mp4', 'video/webm'],
        maxFileSizeBytes: 104857600,
        description: '고화질 비디오 업로드 정책',
      },
      {
        key: 'audio-standard',
        label: '오디오(MP3/WAV)',
        allowedExtensions: ['mp3', 'wav'],
        allowedMimeTypes: ['audio/mpeg', 'audio/wav'],
        maxFileSizeBytes: 20971520,
        description: '오디오 배포용 정책',
      },
      {
        key: 'data-standard',
        label: '데이터 파일(CSV/JSON)',
        allowedExtensions: ['csv', 'json', 'txt'],
        allowedMimeTypes: ['text/csv', 'application/json', 'text/plain'],
        maxFileSizeBytes: 10485760,
        description: '공공 데이터 추출용 정책',
      },
      {
        key: 'archive-standard',
        label: '압축 파일(ZIP/7Z)',
        allowedExtensions: ['zip', '7z'],
        allowedMimeTypes: ['application/zip', 'application/x-7z-compressed'],
        maxFileSizeBytes: 52428800,
        description: '다중 파일 전송용 정책',
      },
    ];

    const retrieveSettings = async () => {
      try {
        const res = await settingsService.get();
        // 초기 데이터 방어 로직
        if (!res.fileUploadDefaults) {
          res.fileUploadDefaults = {
            defaultMaxFileSizeBytes: 10485760,
            defaultMaxRequestSizeBytes: 20971520,
            blockUnmatched: true,
            welcomeMessage: '',
          };
        }
        if (!res.fileTypeTemplates) {
          res.fileTypeTemplates = [];
        }
        settings.value = res;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const save = async () => {
      isSaving.value = true;
      try {
        await settingsService.update(settings.value);
        alertService.showInfo(t$('stackApp.settings.updated', { param: settings.value.id }));
        await retrieveSettings();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };

    // --- 유틸리티 및 이벤트 핸들러 ---

    const getMB = (bytes: number) => {
      if (!bytes) return 0;
      return Math.floor(bytes / (1024 * 1024));
    };

    const updateDefaultMB = (field: string, event: Event) => {
      const input = event.target as HTMLInputElement;
      if (input && settings.value.fileUploadDefaults) {
        settings.value.fileUploadDefaults[field] = Number(input.value) * 1024 * 1024;
      }
    };

    const updatePolicyMB = (policy: any, event: Event) => {
      const input = event.target as HTMLInputElement;
      if (input) {
        policy.maxFileSizeBytes = Number(input.value) * 1024 * 1024;
      }
    };

    const getCommaSeparated = (arr: string[]) => {
      if (!arr) return '';
      return arr.join(', ');
    };

    const updateCommaSeparated = (obj: any, field: string, event: Event) => {
      const input = event.target as HTMLTextAreaElement | HTMLInputElement;
      if (input) {
        obj[field] = input.value
          .split(',')
          .map(s => s.trim())
          .filter(s => s !== '');
      }
    };

    // 정책 관리 로직
    const addPolicy = () => {
      if (!settings.value.fileTypePolicies) {
        settings.value.fileTypePolicies = [];
      }
      settings.value.fileTypePolicies.push({
        key: 'new-policy-' + Date.now(),
        label: '새 정책',
        enabled: true,
        allowedExtensions: [],
        allowedMimeTypes: [],
        maxFileSizeBytes: 10 * 1024 * 1024,
        displayOrder: settings.value.fileTypePolicies.length + 1,
        description: '',
      });
    };

    const removePolicy = (index: any) => {
      settings.value.fileTypePolicies.splice(index as number, 1);
    };

    // 템플릿 관리 로직
    const addTemplate = () => {
      if (!settings.value.fileTypeTemplates) {
        settings.value.fileTypeTemplates = [];
      }
      settings.value.fileTypeTemplates.push({
        key: 'new-template-' + Date.now(),
        label: '새 템플릿',
        allowedExtensions: [],
        allowedMimeTypes: [],
        maxFileSizeBytes: 10 * 1024 * 1024,
        description: '',
      });
    };

    const removeTemplate = (index: number) => {
      settings.value.fileTypeTemplates.splice(index, 1);
    };

    const restoreTemplates = () => {
      if (confirm('모든 추천 템플릿을 초기 기본값으로 복원하시겠습니까? 기존에 수정한 내용은 사라집니다.')) {
        settings.value.fileTypeTemplates = JSON.parse(JSON.stringify(fileTypeTemplates));
      }
    };

    const toggleGuide = () => {
      showGuide.value = !showGuide.value;
    };

    const getTemplateGuide = () => {
      return fileTypeTemplates;
    };

    const applyTemplate = (index: number, template: any) => {
      if (template) {
        if (index === -1) {
          // Quick Add New
          if (!settings.value.fileTypePolicies) {
            settings.value.fileTypePolicies = [];
          }
          const newPolicy = {
            ...template,
            enabled: true,
            displayOrder: settings.value.fileTypePolicies.length + 1,
            description: template.description || '',
            metadata: {},
          };
          settings.value.fileTypePolicies.push(newPolicy);
        } else {
          // Apply to existing
          const target = settings.value.fileTypePolicies[index];
          target.label = template.label;
          target.allowedExtensions = [...template.allowedExtensions];
          target.allowedMimeTypes = [...template.allowedMimeTypes];
          target.maxFileSizeBytes = template.maxFileSizeBytes;
          target.description = template.description;
        }
      }
    };

    const previousState = () => {
      router.go(-1);
    };

    onMounted(() => {
      retrieveSettings();
    });

    return {
      settings,
      isSaving,
      save,
      addPolicy,
      removePolicy,
      addTemplate,
      removeTemplate,
      applyTemplate,
      getMB,
      updateDefaultMB,
      updatePolicyMB,
      getCommaSeparated,
      updateCommaSeparated,
      previousState,
      restoreTemplates,
      toggleGuide,
      showGuide,
      getTemplateGuide,
      t$,
    };
  },
});

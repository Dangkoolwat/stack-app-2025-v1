import { defineComponent, inject, ref, computed, onMounted, onBeforeUnmount, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { type IBoard, Board } from '@/shared/model/board.model';
import BoardService from './board.service';
import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

// Toast UI Editor
import Editor from '@toast-ui/editor';
import '@toast-ui/editor/dist/toastui-editor.css';

export default defineComponent({
  name: 'BoardUpdate',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const boardService = inject('boardService', () => new BoardService(), true);
    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const board: Ref<IBoard> = ref(new Board());
    const boardTypes: Ref<any[]> = ref([]);
    const isSaving = ref(false);

    // 태그 시스템
    const tags: Ref<string[]> = ref([]);

    // 업로드 관리
    const uploads = ref<{ id: number; name: string; size: number }[]>([]);
    const dragover = ref(false);
    const fileInput = ref<HTMLInputElement | null>(null);

    // 에디터
    const editorRef = ref<HTMLElement | null>(null);
    let editorInstance: Editor | null = null;
    let loadedContent = '';

    onMounted(() => {
      initEditor();
    });

    onBeforeUnmount(() => {
      if (editorInstance) {
        editorInstance.destroy();
      }
    });

    // 에디터 마크다운 이미지 추적 로직 (소프트 삭제 연동)
    const extractUploadIds = (text: string): number[] => {
      const regex = /\/api\/uploads\/(\d+)\/preview/g;
      const ids: number[] = [];
      let match;
      while ((match = regex.exec(text)) !== null) {
        ids.push(Number(match[1]));
      }
      return ids;
    };

    let originalImageIds: number[] = [];
    const sessionUploadedImageIds: number[] = [];

    const initEditor = () => {
      if (!editorRef.value) return;
      editorInstance = new Editor({
        el: editorRef.value,
        height: '400px',
        initialEditType: 'wysiwyg',
        previewStyle: 'vertical',
        initialValue: loadedContent || '',
        hooks: {
          addImageBlobHook: (blob: Blob, callback: (url: string, text: string) => void) => {
            // [중요] 동기 함수여야 하며 return false 필수 (Toast UI Editor 3.x 공식 API)
            const formData = new FormData();
            formData.append('file', blob);
            formData.append('public', 'true');
            axios.post('/api/uploads', formData, {
              headers: { 'Content-Type': 'multipart/form-data' }
            }).then(response => {
              const uploadId = response.data.id;
              sessionUploadedImageIds.push(uploadId);
              const imageUrl = `/api/uploads/${uploadId}/preview`;
              const fileName = (blob as File).name || 'image';
              callback(imageUrl, fileName);
            }).catch((err: any) => {
              console.error('Image upload failed', err);
              alertService.showHttpError(err?.response);
            });
            // 기본 base64 삽입 방지 - callback으로만 삽입되도록 함
            return false;
          }
        }
      });
      editorInstance.on('change', () => {
        board.value.content = editorInstance?.getMarkdown() || '';
      });
    };

    const retrieveBoard = async (boardId: number) => {
      try {
        const res = await boardService.get(boardId);
        board.value = res;
        loadedContent = res.content || '';
        originalImageIds = extractUploadIds(loadedContent);
        // 기존 태그 로드
        if (res.tags && res.tags.length > 0) {
          tags.value = [...res.tags];
        }
        // 기존 첨부파일 로드
        if (res.uploads && res.uploads.length > 0) {
          uploads.value = res.uploads.map((u: any) => ({
            id: u.id,
            name: u.sourceFilename || u.name || 'file',
            size: u.fileSize || u.size || 0
          }));
        }
        if (editorInstance) {
          editorInstance.setMarkdown(loadedContent);
        }
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const retrieveBoardTypes = async () => {
      try {
        const res = await commonCodeService.retrieveDetailsByGroup('BOARD_TYPE');
        boardTypes.value = res.data;
      } catch (error: any) {
        console.error('Failed to load board types', error);
      }
    };

    if (route.params?.id) {
      retrieveBoard(Number(route.params.id));
    }
    retrieveBoardTypes();

    const processSoftDeletes = async () => {
      // 최종 마크다운 본문 파싱
      const finalImageIds = extractUploadIds(board.value.content || '');
      // 지워야 할 대상 = (기존 로드된 ID들 + 현재 세션에 업로드한 ID들) 중 최종 본문에 없는 것들
      const allKnownIds = Array.from(new Set([...originalImageIds, ...sessionUploadedImageIds]));
      const idsToDelete = allKnownIds.filter(id => !finalImageIds.includes(id));

      for (const id of idsToDelete) {
        try {
          await axios.delete(`/api/uploads/${id}`);
          console.log(`[Soft Delete] Editor image id=${id} deleted.`);
        } catch (err) {
          console.error(`Failed to soft delete editor image id=${id}`, err);
        }
      }
    };

    const save = async () => {
      isSaving.value = true;
      try {
        // 백엔드 통신 전 로컬 마크다운 추적에서 소실된 이미지 파일 삭제 API 호출
        await processSoftDeletes();

        board.value.tags = tags.value;
        board.value.uploads = uploads.value;

        if (board.value.id) {
          await boardService.update(board.value);
          alertService.showInfo(t$('entities.board.messages.updated', { param: board.value.id }));
        } else {
          const res = await boardService.create(board.value);
          board.value = res;
          alertService.showInfo(t$('entities.board.messages.created', { param: res.id }));
        }
        router.go(-1);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };

    // 파일 업로드 관련 로직
    const triggerFileInput = () => {
      if (fileInput.value) fileInput.value.click();
    };

    const handleFileSelect = (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files) {
        uploadFiles(target.files);
      }
      if (target) target.value = ''; // Reset
    };

    const handleDrop = (event: DragEvent) => {
      dragover.value = false;
      if (event.dataTransfer?.files) {
        uploadFiles(event.dataTransfer.files);
      }
    };

    const uploadFiles = async (files: FileList) => {
      // 제약조건: 최대 5개, 10MB
      for (let i = 0; i < files.length; i++) {
        if (uploads.value.length >= 5) {
          alertService.showError('최대 5개의 파일만 업로드 가능합니다.');
          break;
        }
        const file = files[i];
        if (file.size > 10 * 1024 * 1024) {
          alertService.showError(`파일이 너무 큽니다 (최대 10MB): ${file.name}`);
          continue;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('public', 'true'); // 게시글 첨부는 통상 public입니다.
        try {
          const res = await axios.post('/api/uploads', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
          uploads.value.push({
            id: res.data.id,
            name: file.name,
            size: file.size
          });
        } catch (err: any) {
          alertService.showHttpError(err?.response);
        }
      }
    };

    const removeUpload = async (file: any, index: number) => {
      try {
        // 서버에서 소프트 삭제 처리
        await axios.delete(`/api/uploads/${file.id}`);
        // 화면 리스트에서 제거
        uploads.value.splice(index, 1);
      } catch (err: any) {
        console.error('Failed to soft delete attachment', err);
        alertService.showHttpError(err?.response);
      }
    };

    const formatBytes = (bytes: number) => {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    // 신규 작성 여부
    const isNew = computed(() => !board.value.id);

    return {
      board,
      boardTypes,
      isSaving,
      isNew,
      t$,
      save,
      previousState: () => router.go(-1),
      
      // Editor & Tags
      editorRef,
      tags,
      
      // Upload
      uploads,
      dragover,
      fileInput,
      triggerFileInput,
      handleFileSelect,
      handleDrop,
      removeUpload,
      formatBytes
    };
  },
});

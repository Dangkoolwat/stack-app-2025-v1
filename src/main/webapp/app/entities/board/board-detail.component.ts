import { defineComponent, inject, ref, onMounted, nextTick, onBeforeUnmount, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import { type IBoard } from '@/shared/model/board.model';
import BoardService from './board.service';
import { useAlertService } from '@/shared/alert/alert.service';

// Toast UI Editor (뷰어 모드로 사용)
import Editor from '@toast-ui/editor';
import '@toast-ui/editor/dist/toastui-editor-viewer.css';

export default defineComponent({
  name: 'BoardDetail',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const boardService = inject('boardService', () => new BoardService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const board: Ref<IBoard> = ref({});
    const viewerRef = ref<HTMLElement | null>(null);
    let viewerInstance: any = null;

    // 마크다운 뷰어 초기화 (Editor를 viewer 모드로)
    const initViewer = async (markdown: string) => {
      await nextTick();
      if (!viewerRef.value) return;
      // 기존 뷰어가 있으면 파괴
      if (viewerInstance) {
        viewerInstance.destroy();
        viewerInstance = null;
      }
      try {
        // Toast UI Editor의 Viewer를 dynamic import로 로드
        const ViewerModule = await import('@toast-ui/editor/dist/toastui-editor-viewer');
        const Viewer = ViewerModule.default || ViewerModule;
        viewerInstance = new Viewer({
          el: viewerRef.value,
          initialValue: markdown || '',
        });
      } catch (e) {
        // Viewer 로드 실패 시 fallback: HTML로 직접 삽입
        console.warn('Toast UI Viewer load failed, using fallback', e);
        if (viewerRef.value) {
          // 간단한 마크다운 → HTML 변환 (이미지, 굵은 글씨, 줄바꿈 등)
          viewerRef.value.innerHTML = markdownToHtml(markdown);
        }
      }
    };

    // 간단한 마크다운 → HTML 변환 (fallback용)
    const markdownToHtml = (md: string): string => {
      if (!md) return '';
      let html = md
        // 이미지: ![alt](url) → <img>
        .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width:100%;" />')
        // 링크: [text](url) → <a>
        .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
        // 굵은 글씨: **text** → <strong>
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        // 기울임: *text* → <em>
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        // 헤딩: # → <h1> ~ <h6>
        .replace(/^### (.+)$/gm, '<h3>$1</h3>')
        .replace(/^## (.+)$/gm, '<h2>$1</h2>')
        .replace(/^# (.+)$/gm, '<h1>$1</h1>')
        // 줄바꿈
        .replace(/\n/g, '<br />');
      return html;
    };

    const retrieveBoard = async (boardId: number) => {
      try {
        const res = await boardService.get(boardId);
        board.value = res;
        initViewer(res.content || '');
      } catch (error: any) {
        console.error('Failed to load board', error);
      }
    };

    onBeforeUnmount(() => {
      if (viewerInstance) {
        viewerInstance.destroy();
      }
    });

    if (route.params?.id) {
      retrieveBoard(Number(route.params.id));
    }

    // 파일 확장자에서 아이콘 결정
    const getFileIcon = (filename: string): string => {
      if (!filename) return 'file';
      const ext = filename.split('.').pop()?.toLowerCase() || '';
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)) return 'file-image';
      if (['pdf'].includes(ext)) return 'file-pdf';
      if (['doc', 'docx'].includes(ext)) return 'file-word';
      if (['xls', 'xlsx'].includes(ext)) return 'file-excel';
      if (['ppt', 'pptx'].includes(ext)) return 'file-powerpoint';
      if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'file-archive';
      if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return 'file-video';
      if (['mp3', 'wav', 'ogg'].includes(ext)) return 'file-audio';
      if (['txt', 'md', 'log'].includes(ext)) return 'file-alt';
      return 'file';
    };

    const getFileExtension = (filename: string): string => {
      if (!filename) return '';
      return filename.split('.').pop()?.toUpperCase() || '';
    };

    const formatBytes = (bytes: number) => {
      if (!bytes || bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    return {
      alertService,
      board,
      viewerRef,
      t$,
      previousState: () => router.go(-1),
      getFileIcon,
      getFileExtension,
      formatBytes,
    };
  },
});

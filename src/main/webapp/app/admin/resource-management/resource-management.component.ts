import { defineComponent, ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';

export default defineComponent({
  name: 'ResourceManagement',
  setup() {
    const activeTab = ref('boards'); // "boards" or "uploads"
    const items = ref<any[]>([]);
    const loading = ref(false);
    const selectedIds = ref<number[]>([]);
    const showConfirmModal = ref(false);
    const isHardDeleting = ref(false);

    // 삭제 목록 조회
    const loadItems = async () => {
      loading.value = true;
      items.value = [];
      selectedIds.value = [];
      try {
        const res = await axios.get(`/api/admin/resource-management/${activeTab.value}`);
        items.value = res.data;
      } catch (err) {
        console.error('삭제 목록 조회 실패:', err);
      } finally {
        loading.value = false;
      }
    };

    // 탭 변경 시 자동 조회
    watch(activeTab, () => {
      loadItems();
    });

    onMounted(() => {
      loadItems();
    });

    // 선택 처리
    const toggleSelectAll = (checked: boolean) => {
      if (checked) {
        selectedIds.value = items.value.map(i => i.id);
      } else {
        selectedIds.value = [];
      }
    };

    const isAllSelected = computed(() => {
      return items.value.length > 0 && selectedIds.value.length === items.value.length;
    });

    // 하드 삭제 실행
    const confirmHardDelete = () => {
      if (selectedIds.value.length === 0) return;
      showConfirmModal.value = true;
    };

    const handleHardDelete = async () => {
      isHardDeleting.value = true;
      try {
        await axios.post(`/api/admin/resource-management/${activeTab.value}/hard-delete`, selectedIds.value);
        showConfirmModal.value = false;
        loadItems(); // 목록 갱신
      } catch (err) {
        alert('영구 삭제 중 오류가 발생했습니다.');
        console.error(err);
      } finally {
        isHardDeleting.value = false;
      }
    };

    return {
      activeTab,
      items,
      loading,
      selectedIds,
      showConfirmModal,
      isHardDeleting,
      loadItems,
      toggleSelectAll,
      isAllSelected,
      confirmHardDelete,
      handleHardDelete,
    };
  },
});

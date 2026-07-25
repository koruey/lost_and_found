/**
 * 我的发布页
 */
const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    // Tab：null=全部 0=失物 1=招领
    activeType: null,
    tabList: [
      { label: '全部', value: null },
      { label: '失物', value: 0 },
      { label: '招领', value: 1 }
    ],

    // 列表
    itemList: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    if (!app.globalData.isLoggedIn) {
      app.checkLogin().then(loggedIn => {
        if (loggedIn) this.loadItems();
        else wx.navigateBack();
      });
      return;
    }
    this.loadItems();
  },

  onShow() {
    if (app.globalData.isLoggedIn && this.data.itemList.length > 0) {
      this.refreshList();
    }
  },

  /**
   * 加载我的发布列表
   */
  async loadItems() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });

    try {
      const params = {
        page: this.data.page,
        size: this.data.size
      };
      if (this.data.activeType !== null) {
        params.type = this.data.activeType;
      }

      const result = await api.get('/api/item/my', params);

      if (result && result.records) {
        const newList = this.data.page === 1
          ? result.records
          : [...this.data.itemList, ...result.records];

        this.setData({
          itemList: newList,
          hasMore: this.data.page < result.pages,
          loading: false
        });
      } else {
        this.setData({ loading: false, hasMore: false });
      }
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  /**
   * 刷新列表
   */
  refreshList() {
    this.setData({ page: 1, hasMore: true, itemList: [] });
    this.loadItems();
  },

  /**
   * 加载更多
   */
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadItems();
  },

  /**
   * 切换Tab
   */
  onTabChange(e) {
    const type = e.currentTarget.dataset.type;
    if (type === this.data.activeType) return;
    this.setData({ activeType: type, page: 1, hasMore: true, itemList: [] });
    this.loadItems();
  },

  /**
   * 跳转详情
   */
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/detail/detail?id=' + id });
  },

  /**
   * 标记已解决
   */
  onMarkResolved(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认操作',
      content: '确定标记为已解决吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.put('/api/item/' + id + '/resolved');
            wx.showToast({ title: '已标记', icon: 'success' });
            this.refreshList();
          } catch (err) {
            // ignore
          }
        }
      }
    });
  },

  /**
   * 删除物品
   */
  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定吗？',
      confirmColor: '#FA5151',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.del('/api/item/' + id);
            wx.showToast({ title: '已删除', icon: 'success' });
            this.refreshList();
          } catch (err) {
            // ignore
          }
        }
      }
    });
  },

  /**
   * 下拉刷新
   */
  onRefresh() {
    this.refreshList();
  }
});

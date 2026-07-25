/**
 * 我的收藏页
 */
const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    itemList: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    if (!app.globalData.isLoggedIn) {
      app.checkLogin().then(loggedIn => {
        if (loggedIn) this.loadFavorites();
        else wx.navigateBack();
      });
      return;
    }
    this.loadFavorites();
  },

  onShow() {
    if (app.globalData.isLoggedIn && this.data.itemList.length > 0) {
      this.refreshList();
    }
  },

  /**
   * 加载收藏列表
   */
  async loadFavorites() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });

    try {
      const result = await api.get('/api/favorite', {
        page: this.data.page,
        size: this.data.size
      });

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
    this.loadFavorites();
  },

  /**
   * 加载更多
   */
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadFavorites();
  },

  /**
   * 取消收藏
   */
  onRemoveFavorite(e) {
    const itemId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '取消收藏',
      content: '确定取消收藏该物品吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.del('/api/favorite/' + itemId);
            wx.showToast({ title: '已取消收藏', icon: 'success' });
            // 从列表中移除
            const list = this.data.itemList.filter(item => item.id !== itemId);
            this.setData({ itemList: list });
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

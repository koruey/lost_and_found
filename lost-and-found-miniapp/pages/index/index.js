/**
 * 首页 — 失物招领列表
 */
const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    // Tab：0-失物 1-招领
    activeTab: 1,
    tabTexts: ['失物', '招领'],

    // 分类列表
    categories: [],

    // 当前选中的分类（null=全部）
    activeCategoryId: null,

    // 公告
    announcements: [],
    showAnnounceDetail: false,
    currentAnnouncement: null,

    // 列表数据
    itemList: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    refreshing: false,

    // 登录弹窗
    showLoginModal: false,
    // 是否是首次自动弹出
    autoShowLogin: true
  },

  onLoad() {
    this.loadCategories();
    this.loadItems();
    this.loadAnnouncements();
  },

  onShow() {
    // 每次回到首页都刷新列表（可能从发布页返回或有新数据）
    this.refreshItems();
    this.loadAnnouncements();
  },

  /**
   * 加载公告
   */
  async loadAnnouncements() {
    try {
      const result = await api.get('/api/announcement', { page: 1, size: 5 });
      this.setData({ announcements: result.records || [] });
    } catch (err) {
      // 公告加载失败不影响主流程
    }
  },

  /**
   * 点击公告查看详情
   */
  async onAnnounceTap(e) {
    const id = e.currentTarget.dataset.id;
    try {
      const res = await api.get('/api/announcement/' + id);
      if (res) {
        this.setData({ currentAnnouncement: res, showAnnounceDetail: true });
      }
    } catch (err) {
      wx.showToast({ title: '加载公告详情失败', icon: 'none' });
    }
  },

  /**
   * 关闭公告详情弹窗
   */
  closeAnnounceDetail() {
    this.setData({ showAnnounceDetail: false, currentAnnouncement: null });
  },

  /**
   * 加载分类列表（从服务端动态获取）
   */
  async loadCategories() {
    try {
      const result = await api.get('/api/categories');
      if (result && result.length > 0) {
        const categories = [{ id: null, name: '全部' }, ...result.map(c => ({ id: c.id, name: c.name }))];
        this.setData({ categories });
        app.globalData.categories = categories;
      }
    } catch (err) {
      console.error('加载分类失败，使用本地缓存:', err);
      const cached = app.globalData.categories;
      if (cached && cached.length > 0) {
        this.setData({ categories: cached });
      }
    }
  },

  /**
   * 加载物品列表
   */
  async loadItems() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });

    try {
      const params = {
        type: this.data.activeTab,
        page: this.data.page,
        size: this.data.size,
        status: 1,  // 只查已发布的
        sortBy: 'create_time'
      };

      if (this.data.activeCategoryId) {
        params.categoryId = this.data.activeCategoryId;
      }

      const result = await api.get('/api/item', params);

      if (result && result.records) {
        const newList = this.data.page === 1
          ? result.records
          : [...this.data.itemList, ...result.records];

        this.setData({
          itemList: newList,
          hasMore: this.data.page < result.pages,
          loading: false,
          refreshing: false
        });
      } else {
        this.setData({ loading: false, refreshing: false, hasMore: false });
      }
    } catch (err) {
      console.error('加载物品列表失败:', err);
      this.setData({ loading: false, refreshing: false });
    }
  },

  /**
   * 下拉刷新
   */
  onRefresh() {
    this.setData({ refreshing: true, page: 1, hasMore: true });
    this.loadItems();
  },

  /**
   * 上拉加载更多
   */
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadItems();
  },

  /**
   * 刷新列表（从其他页面返回时）
   */
  refreshItems() {
    this.setData({ page: 1, hasMore: true, itemList: [] });
    this.loadItems();
  },

  /**
   * 切换失物/招领 Tab
   */
  onTabChange(e) {
    const tab = parseInt(e.currentTarget.dataset.tab);
    if (tab === this.data.activeTab) return;
    this.setData({
      activeTab: tab,
      page: 1,
      hasMore: true,
      itemList: []
    });
    this.loadItems();
  },

  /**
   * 选择分类
   */
  onCategoryChange(e) {
    const categoryId = e.currentTarget.dataset.id;
    if (categoryId === this.data.activeCategoryId) return;
    this.setData({
      activeCategoryId: categoryId,
      page: 1,
      hasMore: true,
      itemList: []
    });
    this.loadItems();
  },

  /**
   * 跳转搜索页
   */
  goToSearch() {
    wx.navigateTo({ url: '/pages/search/search' });
  },

  /**
   * 跳转发布页
   */
  goToPublish() {
    app.globalData.publishType = this.data.activeTab;  // 0=失物, 1=招领
    wx.switchTab({ url: '/pages/publish/publish' });
  },

  // ===== 登录相关 =====

  /**
   * 显示登录弹窗
   */
  showLogin() {
    this.setData({ showLoginModal: true, autoShowLogin: false });
  },

  /**
   * 关闭登录弹窗
   */
  closeLoginModal() {
    this.setData({ showLoginModal: false, autoShowLogin: false });
  },

  /**
   * 微信授权登录
   */
  async onGetUserInfo(e) {
    // 新版微信头像昵称获取
    const nickname = e.detail.value ? e.detail.value.nickname || '' : '';
    // 头像由用户自行填写
    try {
      await app.doLogin(nickname, '');
      this.setData({ showLoginModal: false, autoShowLogin: false });
      wx.showToast({ title: '登录成功', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: '登录失败，请重试', icon: 'none' });
    }
  },

  /**
   * 使用微信一键登录
   */
  onWxLogin() {
    const that = this;
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: async (res) => {
        const userInfo = res.userInfo;
        try {
          await app.doLogin(userInfo.nickName, userInfo.avatarUrl);
          that.setData({ showLoginModal: false, autoShowLogin: false });
          wx.showToast({ title: '登录成功', icon: 'success' });
        } catch (err) {
          // 登录失败，仍关闭弹窗（用户可浏览内容）
          that.setData({ showLoginModal: false, autoShowLogin: false });
          wx.showToast({ title: '登录失败，请重试', icon: 'none' });
        }
      },
      fail: () => {
        // 用户拒绝授权，关闭弹窗
        that.setData({ showLoginModal: false, autoShowLogin: false });
      }
    });
  },

  /**
   * 暂不登录，关闭弹窗
   */
  onSkipLogin() {
    this.setData({ showLoginModal: false, autoShowLogin: false });
  }
});

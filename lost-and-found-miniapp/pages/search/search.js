/**
 * 搜索页 — 关键词搜索 + 多条件筛选
 */
const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    // 搜索关键词
    keyword: '',

    // 筛选条件
    categoryId: null,
    categoryName: '',
    location: '',
    startDate: '',
    endDate: '',

    // 失物/招领
    itemType: null,

    // 排序
    sortBy: 'create_time',

    // 分类列表
    categories: [],

    // 筛选面板
    showFilterPanel: false,

    // 历史搜索
    searchHistory: [],

    // 结果列表
    resultList: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    searched: false   // 是否已执行过搜索
  },

  onLoad() {
    this.initCategories();
    this.loadHistory();
  },

  /**
   * 初始化分类（从服务端动态获取）
   */
  async initCategories() {
    try {
      const result = await api.get('/api/categories');
      if (result && result.length > 0) {
        const categories = [{ id: null, name: '全部分类' }, ...result.map(c => ({ id: c.id, name: c.name }))];
        this.setData({ categories });
        app.globalData.categories = categories;
      }
    } catch (err) {
      const cached = app.globalData.categories;
      if (cached && cached.length > 0) {
        this.setData({ categories: cached });
      } else {
        this.setData({ categories: [
          { id: null, name: '全部分类' },
          { id: 1, name: '手机' }, { id: 2, name: '耳机' },
          { id: 3, name: '钥匙' }, { id: 4, name: '校园卡' },
          { id: 5, name: '钱包' }, { id: 6, name: '书籍' },
          { id: 7, name: '雨伞' }, { id: 8, name: '电脑' },
          { id: 9, name: '眼镜' }, { id: 10, name: '水杯' },
          { id: 11, name: '身份证' }, { id: 12, name: '衣物' },
          { id: 13, name: '背包' }, { id: 14, name: '文具' },
          { id: 15, name: '其他' }
        ]});
      }
    }
  },

  /**
   * 加载搜索历史
   */
  loadHistory() {
    try {
      const history = wx.getStorageSync('searchHistory') || [];
      this.setData({ searchHistory: history.slice(0, 10) });
    } catch (e) {
      // ignore
    }
  },

  /**
   * 保存搜索关键词
   */
  saveKeyword(keyword) {
    if (!keyword.trim()) return;
    let history = wx.getStorageSync('searchHistory') || [];
    history = history.filter(h => h !== keyword);
    history.unshift(keyword);
    history = history.slice(0, 10);
    wx.setStorageSync('searchHistory', history);
    this.setData({ searchHistory: history });
  },

  // ===== 输入处理 =====

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearchConfirm(e) {
    const keyword = e.detail.value.trim();
    if (!keyword) return;
    this.setData({ keyword });
    this.saveKeyword(keyword);
    this.doSearch();
  },

  onLocationInput(e) {
    this.setData({ location: e.detail.value });
  },

  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value });
  },

  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value });
  },

  onCategoryChange(e) {
    const index = e.detail.value;
    const cat = this.data.categories[index];
    this.setData({ categoryId: cat.id, categoryName: cat.name });
  },

  onSortChange(e) {
    this.setData({ sortBy: e.currentTarget.dataset.sort });
    this.doSearch();
  },

  // ===== 搜索 =====

  /**
   * 点击搜索按钮
   */
  onSearch() {
    const keyword = this.data.keyword.trim();
    if (keyword) {
      this.saveKeyword(keyword);
    }
    this.doSearch();
  },

  /**
   * 执行搜索
   */
  async doSearch() {
    this.setData({ loading: true, searched: true, page: 1, resultList: [] });

    try {
      const params = {
        page: this.data.page,
        size: this.data.size,
        status: 1,
        sortBy: this.data.sortBy
      };

      if (this.data.keyword.trim()) params.keyword = this.data.keyword.trim();
      if (this.data.itemType !== null) params.type = this.data.itemType;
      if (this.data.categoryId) params.categoryId = this.data.categoryId;
      if (this.data.location.trim()) params.location = this.data.location.trim();
      if (this.data.startDate) params.startDate = this.data.startDate;
      if (this.data.endDate) params.endDate = this.data.endDate;

      const result = await api.get('/api/item', params);

      if (result && result.records) {
        this.setData({
          resultList: result.records,
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
   * 点击历史搜索关键词
   */
  onHistoryTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this.doSearch();
  },

  /**
   * 清除搜索历史
   */
  onClearHistory() {
    wx.showModal({
      title: '清除历史',
      content: '确定清除所有搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory');
          this.setData({ searchHistory: [] });
        }
      }
    });
  },

  /**
   * 清除筛选
   */
  onClearFilter() {
    this.setData({
      categoryId: null,
      categoryName: '',
      location: '',
      startDate: '',
      endDate: '',
      itemType: null,
      sortBy: 'create_time',
      showFilterPanel: false
    });
  },

  /**
   * 切换筛选面板
   */
  toggleFilter() {
    this.setData({ showFilterPanel: !this.data.showFilterPanel });
  },

  /**
   * 加载更多
   */
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadMore();
  },

  async loadMore() {
    this.setData({ loading: true });

    try {
      const params = {
        page: this.data.page,
        size: this.data.size,
        status: 1,
        sortBy: this.data.sortBy
      };

      if (this.data.keyword.trim()) params.keyword = this.data.keyword.trim();
      if (this.data.itemType !== null) params.type = this.data.itemType;
      if (this.data.categoryId) params.categoryId = this.data.categoryId;
      if (this.data.location.trim()) params.location = this.data.location.trim();
      if (this.data.startDate) params.startDate = this.data.startDate;
      if (this.data.endDate) params.endDate = this.data.endDate;

      const result = await api.get('/api/item', params);

      if (result && result.records) {
        this.setData({
          resultList: [...this.data.resultList, ...result.records],
          hasMore: this.data.page < result.pages,
          loading: false
        });
      } else {
        this.setData({ loading: false, hasMore: false });
      }
    } catch (err) {
      this.setData({ loading: false });
    }
  }
});

/**
 * 发布页面 — 失物/招领发布 + AI辅助
 */
const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    // 编辑模式
    editId: null,
    isEdit: false,

    // 物品类型：0-失物 1-招领
    itemType: 1,

    // 表单字段
    title: '',
    description: '',
    categoryId: null,
    categoryName: '',
    categoryIndex: '',
    location: '',
    itemDate: '',
    contact: '',

    // 图片
    imageUrls: [],

    // 分类列表
    categories: [],

    // AI辅助
    aiPanelExpanded: false,
    aiLoading: false,
    aiResults: {
      category: '',      // AI识别的分类名
      description: '',   // AI生成的描述
      ocrText: '',       // OCR识别文字
      enhanced: ''       // 增强描述
    },
    aiUsed: false,

    // 提交状态
    submitting: false
  },

  onLoad(options) {
    // 日期上限设为今天
    const today = new Date();
    const y = today.getFullYear();
    const m = (today.getMonth() + 1).toString().padStart(2, '0');
    const d = today.getDate().toString().padStart(2, '0');
    this.setData({ today: `${y}-${m}-${d}` });

    // 根据首页传来的类型设置默认失物/招领
    if (app.globalData.publishType !== undefined) {
      this.setData({ itemType: app.globalData.publishType });
      delete app.globalData.publishType;
    }

    this.initCategories();

    // 编辑模式
    if (options.editId) {
      this.setData({ editId: options.editId, isEdit: true });
      wx.setNavigationBarTitle({ title: '编辑物品' });
      this.loadItemForEdit(options.editId);
    }
  },

  onShow() {
    // 从首页"去发布"跳转过来时，切换到对应类型
    if (app.globalData.publishType !== undefined) {
      this.setData({ itemType: parseInt(app.globalData.publishType) });
      delete app.globalData.publishType;
    }
  },

  /**
   * 加载分类列表（从服务端动态获取）
   */
  async initCategories() {
    try {
      const result = await api.get('/api/categories');
      if (result && result.length > 0) {
        const categories = result.map(c => ({ id: c.id, name: c.name }));
        this.setData({ categories });
        app.globalData.categories = categories;
      }
    } catch (err) {
      // 网络不可用时使用本地缓存，再无则用硬编码兜底
      const cached = app.globalData.categories;
      if (cached && cached.length > 0) {
        this.setData({ categories: cached });
      } else {
        this.setData({ categories: [
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
   * 编辑模式：加载原有物品数据
   */
  async loadItemForEdit(itemId) {
    try {
      const item = await api.get('/api/item/' + itemId);
      const categories = this.data.categories;
      const catIndex = categories.findIndex(c => c.id === item.categoryId);
      this.setData({
        itemType: item.type,
        title: item.title || '',
        description: item.description || '',
        categoryId: item.categoryId,
        categoryName: item.categoryName || '',
        categoryIndex: catIndex >= 0 ? catIndex : '',
        location: item.location || '',
        itemDate: item.itemDate || '',
        contact: item.contact || '',
        imageUrls: item.images || []
      });
    } catch (err) {
      wx.showToast({ title: '加载物品信息失败', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  // ===== 表单输入处理 =====

  onTypeChange(e) {
    this.setData({ itemType: parseInt(e.currentTarget.dataset.type) });
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value });
  },

  onDescriptionInput(e) {
    this.setData({ description: e.detail.value });
  },

  onCategoryChange(e) {
    const index = e.detail.value;
    const category = this.data.categories[index];
    this.setData({
      categoryIndex: index,
      categoryId: category.id,
      categoryName: category.name
    });
  },

  onLocationInput(e) {
    this.setData({ location: e.detail.value });
  },

  onDateChange(e) {
    this.setData({ itemDate: e.detail.value });
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value });
  },

  // ===== 图片上传 =====

  onImageChange(e) {
    this.setData({ imageUrls: e.detail.urls });

    // 图片上传完成后自动显示AI面板提示
    if (e.detail.urls && e.detail.urls.length > 0 && !this.data.aiUsed) {
      // 不自动调用，但提示用户可以AI识别
    }
  },

  // ===== AI辅助功能 =====

  /**
   * 展开/收起AI面板
   */
  toggleAiPanel() {
    if (!this.data.imageUrls || this.data.imageUrls.length === 0) {
      wx.showToast({ title: '请先上传图片', icon: 'none' });
      return;
    }
    this.setData({ aiPanelExpanded: !this.data.aiPanelExpanded });
  },

  /**
   * AI智能识别（并行调用3个API）
   */
  async onAiRecognize() {
    if (!this.data.imageUrls || this.data.imageUrls.length === 0) {
      wx.showToast({ title: '请先上传图片', icon: 'none' });
      return;
    }

    this.setData({ aiLoading: true, aiPanelExpanded: true });
    const firstImage = this.data.imageUrls[0];

    try {
      // 并行调用3个AI接口
      const results = await Promise.allSettled([
        api.post('/api/ai/recognize', { imageUrl: firstImage }),
        api.post('/api/ai/describe', { imageUrl: firstImage }),
        api.post('/api/ai/ocr', { imageUrl: firstImage })
      ]);

      // 解析结果
      let category = '';
      let description = '';
      let ocrText = '';

      if (results[0].status === 'fulfilled' && results[0].value) {
        category = results[0].value.category || '';
      }
      if (results[1].status === 'fulfilled' && results[1].value) {
        description = results[1].value.description || '';
      }
      if (results[2].status === 'fulfilled' && results[2].value) {
        ocrText = results[2].value.text || '';
      }

      this.setData({
        'aiResults.category': category,
        'aiResults.description': description,
        'aiResults.ocrText': ocrText,
        aiLoading: false,
        aiUsed: true
      });

      wx.showToast({ title: 'AI识别完成', icon: 'success' });
    } catch (err) {
      this.setData({ aiLoading: false });
      wx.showToast({ title: 'AI识别失败，请重试', icon: 'none' });
    }
  },

  /**
   * 调用AI增强描述
   */
  async onAiEnhance() {
    const { imageUrls, description } = this.data;
    if (!imageUrls || imageUrls.length === 0) {
      wx.showToast({ title: '请先上传图片', icon: 'none' });
      return;
    }

    this.setData({ aiLoading: true });
    const firstImage = imageUrls[0];

    try {
      const res = await api.post('/api/ai/enhance', {
        imageUrl: firstImage,
        userText: description || ''
      });

      this.setData({
        'aiResults.enhanced': res.enhanced || '',
        aiLoading: false
      });
      wx.showToast({ title: '增强描述完成', icon: 'success' });
    } catch (err) {
      this.setData({ aiLoading: false });
      wx.showToast({ title: '增强描述失败', icon: 'none' });
    }
  },

  /**
   * 应用AI分类
   */
  onApplyAiCategory() {
    const aiCategory = this.data.aiResults.category;
    if (!aiCategory) return;

    // 模糊匹配分类
    const categories = this.data.categories;
    const match = categories.find(c =>
      c.name.includes(aiCategory) || aiCategory.includes(c.name)
    );
    if (match) {
      this.setData({ categoryId: match.id, categoryName: match.name });
      wx.showToast({ title: '已应用AI分类: ' + match.name, icon: 'success' });
    } else {
      wx.showToast({ title: '未能匹配分类，请手动选择', icon: 'none' });
    }
  },

  /**
   * 应用AI描述
   */
  onApplyAiDescription() {
    const aiDesc = this.data.aiResults.description;
    if (!aiDesc) return;
    this.setData({ description: aiDesc });
    wx.showToast({ title: '已应用AI描述', icon: 'success' });
  },

  /**
   * 应用增强描述
   */
  onApplyEnhanced() {
    const enhanced = this.data.aiResults.enhanced;
    if (!enhanced) return;
    this.setData({ description: enhanced });
    wx.showToast({ title: '已应用增强描述', icon: 'success' });
  },

  // ===== 表单验证 =====

  validate() {
    const { title, description, categoryId, location, itemDate, imageUrls } = this.data;

    if (!imageUrls || imageUrls.length === 0) {
      wx.showToast({ title: '请上传至少一张图片', icon: 'none' });
      return false;
    }

    if (!title.trim()) {
      wx.showToast({ title: '请输入标题', icon: 'none' });
      return false;
    }
    if (title.trim().length < 2) {
      wx.showToast({ title: '标题至少2个字符', icon: 'none' });
      return false;
    }

    if (!description.trim()) {
      wx.showToast({ title: '请输入描述', icon: 'none' });
      return false;
    }
    if (description.trim().length < 10) {
      wx.showToast({ title: '描述至少10个字符', icon: 'none' });
      return false;
    }

    if (!categoryId) {
      wx.showToast({ title: '请选择分类', icon: 'none' });
      return false;
    }

    if (!location.trim()) {
      wx.showToast({ title: '请输入地点', icon: 'none' });
      return false;
    }

    if (!itemDate) {
      wx.showToast({ title: '请选择日期', icon: 'none' });
      return false;
    }

    return true;
  },

  // ===== 提交 =====

  async onSubmit() {
    if (!app.globalData.isLoggedIn) {
      app.checkLogin().then(loggedIn => {
        if (loggedIn) this.onSubmit();
      });
      return;
    }

    if (!this.validate()) return;
    if (this.data.submitting) return;

    this.setData({ submitting: true });

    const payload = {
      type: this.data.itemType,
      title: this.data.title.trim(),
      description: this.data.description.trim(),
      categoryId: this.data.categoryId,
      location: this.data.location.trim(),
      itemDate: this.data.itemDate,
      contact: this.data.contact.trim(),
      images: this.data.imageUrls
    };

    try {
      if (this.data.isEdit) {
        await api.put('/api/item/' + this.data.editId, payload);
        wx.showToast({ title: '修改成功', icon: 'success' });
      } else {
        await api.post('/api/item', payload);
        wx.showToast({ title: '发布成功', icon: 'success' });
      }

      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' });
      }, 1500);
    } catch (err) {
      this.setData({ submitting: false });
    }
  }
});

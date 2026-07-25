/**
 * 物品卡片组件
 * 用于列表展示，显示物品摘要信息
 */
const util = require('../../utils/util.js');

Component({
  properties: {
    // 物品数据（ItemListResponse 格式）
    item: {
      type: Object,
      value: {},
      observer: '_onItemChange'
    },
    // 是否显示类型标签（失物/招领）
    showType: {
      type: Boolean,
      value: false
    }
  },

  data: {
    title: '',
    firstImage: '',
    categoryName: '',
    location: '',
    timeAgo: '',
    viewCount: 0,
    commentCount: 0,
    typeText: '',
    typeClass: ''
  },

  methods: {
    _onItemChange(item) {
      if (!item || !item.id) return;

      this.setData({
        title: item.title || '',
        firstImage: util.getImageUrl(item.firstImage),
        categoryName: item.categoryName || '',
        location: item.location || '',
        timeAgo: util.formatTimeAgo(item.createdAt),
        viewCount: item.viewCount || 0,
        commentCount: item.commentCount || 0,
        typeText: item.type === 0 ? '失物' : '招领',
        typeClass: item.type === 0 ? 'tag-orange' : 'tag-green'
      });
    },

    /**
     * 点击卡片，跳转详情页
     */
    onTap() {
      const item = this.properties.item;
      if (item && item.id) {
        wx.navigateTo({
          url: '/pages/detail/detail?id=' + item.id
        });
      }
    }
  }
});

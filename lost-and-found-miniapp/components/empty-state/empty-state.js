/**
 * 空状态组件
 * 用于列表无数据时的占位展示
 */
Component({
  properties: {
    // 提示图标/emoji
    icon: {
      type: String,
      value: '📭'
    },
    // 提示文字
    text: {
      type: String,
      value: '暂无数据'
    },
    // 操作按钮文字（不传则不显示按钮）
    actionText: {
      type: String,
      value: ''
    }
  },

  methods: {
    /**
     * 点击操作按钮
     */
    onAction() {
      this.triggerEvent('action');
    }
  }
});

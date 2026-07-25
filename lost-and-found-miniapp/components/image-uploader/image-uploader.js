/**
 * 图片上传组件
 * 多图选择 → 逐张上传 → 网格展示 → 支持删除
 */
const api = require('../../utils/api.js');

Component({
  properties: {
    // 最大图片数量
    max: {
      type: Number,
      value: 9
    },
    // 已有图片URL数组（编辑模式）
    value: {
      type: Array,
      value: [],
      observer: '_onValueChange'
    }
  },

  data: {
    images: [],          // 已上传的图片URL列表
    uploading: false,    // 是否正在上传
    uploadCount: 0       // 正在上传中的数量
  },

  methods: {
    _onValueChange(newVal) {
      if (newVal && newVal.length > 0) {
        this.setData({ images: [...newVal] });
      }
    },

    /**
     * 选择图片
     */
    onChooseImage() {
      const { images, max } = this.data;
      const remain = max - images.length;

      if (remain <= 0) {
        wx.showToast({ title: '最多上传' + max + '张图片', icon: 'none' });
        return;
      }

      wx.chooseImage({
        count: remain,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this._uploadImages(res.tempFilePaths);
        },
        fail: (err) => {
          if (err.errMsg !== 'chooseImage:fail cancel') {
            wx.showToast({ title: '选择图片失败', icon: 'none' });
          }
        }
      });
    },

    /**
     * 批量上传图片（压缩后并发上传）
     */
    async _uploadImages(tempFiles) {
      this.setData({ uploading: true, uploadCount: tempFiles.length });

      // 先压缩所有图片
      const compressedFiles = await Promise.all(
        tempFiles.map(filePath => new Promise((resolve) => {
          wx.compressImage({
            src: filePath,
            quality: 80,
            success: (res) => resolve(res.tempFilePath),
            fail: () => resolve(filePath)  // 压缩失败用原图
          });
        }))
      );

      // 并发上传所有图片
      const results = await Promise.allSettled(
        compressedFiles.map(filePath => api.uploadImage(filePath))
      );

      const uploadedUrls = [];
      let failCount = 0;
      results.forEach(r => {
        if (r.status === 'fulfilled' && r.value) {
          uploadedUrls.push(r.value);
        } else {
          failCount++;
        }
      });

      // 合并已有图片
      const allImages = [...this.data.images, ...uploadedUrls];
      this.setData({
        images: allImages,
        uploading: false,
        uploadCount: 0
      });

      if (failCount > 0) {
        wx.showToast({ title: failCount + '张图片上传失败', icon: 'none' });
      }

      // 通知父组件
      this.triggerEvent('change', { urls: allImages });
    },

    /**
     * 预览图片
     */
    onPreviewImage(e) {
      const index = e.currentTarget.dataset.index;
      wx.previewImage({
        current: this.data.images[index],
        urls: this.data.images
      });
    },

    /**
     * 删除图片
     */
    onDeleteImage(e) {
      const index = e.currentTarget.dataset.index;
      const images = [...this.data.images];
      images.splice(index, 1);
      this.setData({ images });
      this.triggerEvent('change', { urls: images });
    },

    /**
     * 获取当前图片列表（供父组件调用）
     */
    getImages() {
      return this.data.images;
    }
  }
});

/**
 * 通用工具函数
 */

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @returns {string} 如 "2026-07-22"
 */
function formatDate(date) {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return year + '-' + month + '-' + day;
}

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间
 * @returns {string} 如 "2026-07-22 14:30"
 */
function formatDateTime(dateTime) {
  if (!dateTime) return '';
  const d = new Date(dateTime);
  return formatDate(d) + ' ' +
    String(d.getHours()).padStart(2, '0') + ':' +
    String(d.getMinutes()).padStart(2, '0');
}

/**
 * 智能时间显示
 * @param {string|Date} dateTime - 日期时间
 * @returns {string} "刚刚" / "x分钟前" / "x小时前" / "x天前" / 完整日期
 */
function formatTimeAgo(dateTime) {
  if (!dateTime) return '';
  const now = Date.now();
  const target = new Date(dateTime).getTime();
  const diff = now - target;

  if (diff < 0) return formatDate(dateTime);

  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) return '刚刚';
  if (diff < hour) return Math.floor(diff / minute) + '分钟前';
  if (diff < day) return Math.floor(diff / hour) + '小时前';
  if (diff < 7 * day) return Math.floor(diff / day) + '天前';
  return formatDate(dateTime);
}

/**
 * 获取图片完整URL
 * @param {string} url - 图片路径
 * @returns {string} 完整URL
 */
function getImageUrl(url) {
  if (!url) return '/assets/default-image.png';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return getApp().globalData.apiBase + url;
}

/**
 * 文本截断
 * @param {string} text - 原文本
 * @param {number} maxLen - 最大长度
 * @returns {string}
 */
function truncateText(text, maxLen) {
  if (!text) return '';
  if (text.length <= maxLen) return text;
  return text.substring(0, maxLen) + '...';
}

/**
 * 防抖
 * @param {Function} fn - 回调函数
 * @param {number} delay - 延迟(ms)
 * @returns {Function}
 */
function debounce(fn, delay) {
  let timer = null;
  return function () {
    const context = this;
    const args = arguments;
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(context, args);
    }, delay);
  };
}

/**
 * 获取物品类型文本
 * @param {number} type - 0失物 1招领
 * @returns {string}
 */
function getItemTypeText(type) {
  return type === 0 ? '失物' : '招领';
}

/**
 * 获取物品状态文本
 * @param {number} status - 0待审 1发布 2驳回 3解决
 * @returns {string}
 */
function getItemStatusText(status) {
  const map = { 0: '待审核', 1: '已发布', 2: '审核不通过', 3: '已解决' };
  return map[status] || '未知';
}

/**
 * 获取通知类型图标
 * @param {number} type - 0匹配 1评论 2审核 3系统
 * @returns {string} 图标名称
 */
function getNotifyTypeIcon(type) {
  const map = { 0: '🔔', 1: '💬', 2: '📋', 3: '📢' };
  return map[type] || '📌';
}

module.exports = {
  formatDate,
  formatDateTime,
  formatTimeAgo,
  getImageUrl,
  truncateText,
  debounce,
  getItemTypeText,
  getItemStatusText,
  getNotifyTypeIcon
};

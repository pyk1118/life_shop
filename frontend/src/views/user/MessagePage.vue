<template>
  <div class="message-page">
    <header class="page-header">
      <h2><a @click.prevent="goBack" style="color:black; cursor:pointer; text-decoration:none; margin-right:8px;">&lt;
    </a>留言反馈</h2>
    </header>

    <div class="message-content">
      <transition name="list-fade">
        <div v-if="isUser" class="submit-section">
          <h3>发布留言</h3>
          <div class="form-field">
            <label class="field-label">留言标题</label>
            <el-input 
              v-model="msgForm.messageTitle" 
              placeholder="请输入留言标题，输入@可关联店铺/开发者，输入#可关联商品" 
              @input="handleTitleInput"
            />
            <transition name="fade">
              <div v-if="showShopSuggestions" class="suggestion-dropdown">
                <div class="suggestion-item developer-item" @click="insertDeveloper">
                  <span class="suggestion-prefix">@</span><span class="developer-name">开发者</span>
                </div>
                <div 
                  v-for="shop in shopSuggestions" 
                  :key="shop.shopId" 
                  class="suggestion-item"
                  @click="insertShop(shop)"
                >
                  <span class="suggestion-prefix">@</span>{{ shop.shopName }}
                </div>
              </div>
              <div v-else-if="showGoodsSuggestions" class="suggestion-dropdown">
                <div 
                  v-for="goods in goodsSuggestions" 
                  :key="goods.goodsId" 
                  class="suggestion-item"
                  @click="insertGoods(goods)"
                >
                  <span class="suggestion-prefix">#</span>{{ goods.goodsName }}
                </div>
              </div>
            </transition>
          </div>
          <div class="form-field">
            <label class="field-label">留言内容</label>
            <el-input v-model="msgForm.messageContent" type="textarea" :rows="4" placeholder="请输入留言内容" />
          </div>
          <el-form-item>
            <button type="button" class="gradient-btn apply-btn" @click="submitMessage">发布留言</button>
          </el-form-item>
        </div>
        <div v-else class="guest-tip">
          <el-alert title="游客提示" type="info" :closable="false" show-icon>
            <template #default>
              <p>您当前以游客身份访问，仅可浏览留言。如需发布留言或评论，请先登录。</p>
            </template>
          </el-alert>
        </div>
      </transition>

      <div class="message-list-header">
        <h3>全部留言</h3>
        <div class="sort-bar">
          <el-radio-group v-model="sortType" size="small" @change="loadMessages">
            <el-radio-button value="time">最新发布</el-radio-button>
            <el-radio-button value="comments">评论最多</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <transition-group name="list-fade">
        <div v-for="msg in messages" :key="msg.messageId" class="message-card">
          <div class="message-header">
            <div class="user-info">
              <div class="avatar">
                <img v-if="msg.userAvatar" :src="msg.userAvatar" alt="头像" @error="handleAvatarError" />
                <span v-else>{{ msg.userAccount?.charAt(0)?.toUpperCase() || 'U' }}</span>
              </div>
              <div class="user-detail">
                <span class="user-name">{{ msg.userAccount }}</span>
                <span class="publish-time">{{ formatTime(msg.createTime) }}</span>
              </div>
            </div>
          </div>
          <div class="message-body">
            <div class="message-title">
              <span v-html="highlightTag(msg.messageTitle)"></span>
            </div>
            <div class="message-content-text">{{ msg.messageContent }}</div>
          </div>
          <div class="message-footer">
            <span class="reply-count">{{ msg.commentCount || 0 }} 条评论</span>
            <button class="comment-btn" @click="toggleComments(msg)">
              <span>{{ showComments[msg.messageId] ? '收起' : '查看评论' }}</span>
            </button>
          </div>

          <transition name="expand">
            <div v-if="showComments[msg.messageId]" class="comments-section">
              <div v-if="isUser" class="comment-input-area">
                <el-input 
                  v-model="commentForms[msg.messageId]" 
                  placeholder="写下你的评论..." 
                  @keyup.enter="submitComment(msg.messageId)"
                />
                <button class="send-comment-btn" @click="submitComment(msg.messageId)">发送</button>
              </div>

              <div v-if="msg.comments && msg.comments.length > 0" class="comments-list">
                <div v-for="comment in msg.comments" :key="comment.commentId" class="comment-item" :class="{ 'is-top': comment.isTop === 1, 'is-merchant': comment.isTop === 2 }">
                  <div class="comment-avatar" :class="{ 'merchant-avatar': comment.isTop === 2 }">
                    <img v-if="comment.userAvatar" :src="comment.userAvatar" alt="头像" @error="handleAvatarError" />
                    <span v-else>{{ comment.isTop === 1 ? 'A' : comment.isTop === 2 ? 'S' : (comment.userAccount?.charAt(0)?.toUpperCase() || 'U') }}</span>
                  </div>
                  <div class="comment-content">
                    <div class="comment-header">
                      <span class="comment-user" :class="{ 'admin-user': comment.isTop === 1, 'merchant-user': comment.isTop === 2 }">{{ comment.userAccount }}</span>
                      <el-tag v-if="comment.isTop === 1" size="small" type="danger" class="top-tag">置顶</el-tag>
                      <el-tag v-if="comment.isTop === 2" size="small" type="success" class="top-tag">商家回复</el-tag>
                      <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <p>{{ comment.commentContent }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="no-comments">暂无评论，快来发表第一条评论吧~</div>
            </div>
          </transition>
        </div>
      </transition-group>

      <el-empty v-if="!loading && !messages.length" description="暂无留言" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Message } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { createMessage, createComment } from '../../api/user';
import { getPublicMessageList, getShopList, getGoodsPage, getMessageComments } from '../../api/home';
import { getRole } from '../../utils/auth';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
userStore.syncFromStorage();
const router = useRouter();

const messages = ref([]);
const loading = ref(false);
const msgForm = reactive({ messageTitle: '', messageContent: '' });
const isUser = computed(() => getRole() === 'USER');
const sortType = ref('time');
const showComments = ref({});
const commentForms = ref({});

const showShopSuggestions = ref(false);
const showGoodsSuggestions = ref(false);
const shopSuggestions = ref([]);
const goodsSuggestions = ref([]);
const allShops = ref([]);
const allGoods = ref([]);
const lastAtIndex = ref(-1);
const lastHashIndex = ref(-1);
const currentShopId = ref(null);

function goBack() {
  router.back();
}

function formatTime(dateStr) {
  if (!dateStr) {
    return '';
  }
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) {
    return '刚刚';
  }
  if (minutes < 60) {
    return `${minutes}分钟前`;
  }
  if (hours < 24) {
    return `${hours}小时前`;
  }
  if (days < 30) {
    return `${days}天前`;
  }
  return date.toLocaleDateString('zh-CN');
}

function highlightTag(text) {
  if (!text) {
    return '';
  }
  return text
    .replace(/@(\S+)/g, '<span class="tag-shop">@$1</span>')
    .replace(/#(\S+)/g, '<span class="tag-goods">#$1</span>');
}

async function loadMessages() {
  loading.value = true;
  try {
    const sortParam = sortType.value === 'comments' ? 'commentCount' : '';
    const res = await getPublicMessageList({ sort: sortParam });
    messages.value = (res.data || []).filter(msg => !msg.parentId);
    showComments.value = {};
  } catch (e) {
    console.error('加载留言失败:', e);
    messages.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadShops() {
  try {
    const res = await getShopList();
    allShops.value = res.data || [];
  } catch (e) {
    console.error('加载店铺列表失败:', e);
  }
}

async function loadGoods() {
  try {
    const res = await getGoodsPage({ current: 1, size: 100 });
    allGoods.value = res.data?.records || [];
  } catch (e) {
    console.error('加载商品列表失败:', e);
  }
}

function handleTitleInput() {
  const title = msgForm.messageTitle;
  const atIndex = title.lastIndexOf('@');
  const hashIndex = title.lastIndexOf('#');
  
  if (atIndex >= 0 && (hashIndex < 0 || atIndex > hashIndex)) {
    const searchText = title.substring(atIndex + 1);
    if (atIndex > lastAtIndex.value) {
      lastAtIndex.value = atIndex;
      lastHashIndex.value = -1;
      currentShopId.value = null;
      showGoodsSuggestions.value = false;
    }
    
    if (searchText.length >= 0) {
      showShopSuggestions.value = true;
      shopSuggestions.value = allShops.value.filter(s => s.shopName.includes(searchText));
    }
  } else if (hashIndex >= 0 && (atIndex < 0 || hashIndex > atIndex)) {
    const searchText = title.substring(hashIndex + 1);
    if (hashIndex > lastHashIndex.value) {
      lastHashIndex.value = hashIndex;
      showShopSuggestions.value = false;
    }
    
    if (searchText.length >= 0) {
      showGoodsSuggestions.value = true;
      if (currentShopId.value) {
        goodsSuggestions.value = allGoods.value.filter(g => g.shopId === currentShopId.value && g.goodsName.includes(searchText));
      } else {
        goodsSuggestions.value = [];
      }
    }
  } else {
    showShopSuggestions.value = false;
    showGoodsSuggestions.value = false;
  }
}

function insertShop(shop) {
  const title = msgForm.messageTitle;
  const atIndex = title.lastIndexOf('@');
  msgForm.messageTitle = title.substring(0, atIndex) + `@${shop.shopName} `;
  showShopSuggestions.value = false;
  currentShopId.value = shop.shopId;
}

function insertDeveloper() {
  const title = msgForm.messageTitle;
  const atIndex = title.lastIndexOf('@');
  msgForm.messageTitle = title.substring(0, atIndex) + `@开发者 `;
  showShopSuggestions.value = false;
  currentShopId.value = null;
}

function insertGoods(goods) {
  const title = msgForm.messageTitle;
  const hashIndex = title.lastIndexOf('#');
  msgForm.messageTitle = title.substring(0, hashIndex) + `#${goods.goodsName} `;
  showGoodsSuggestions.value = false;
}

async function submitMessage() {
  if (!msgForm.messageTitle || !msgForm.messageContent) {
    ElMessage.warning('请填写完整留言信息');
    return;
  }
  try {
    await createMessage(msgForm);
    ElMessage.success('留言提交成功');
    msgForm.messageTitle = '';
    msgForm.messageContent = '';
    loadMessages();
  } catch (e) {
    ElMessage.error('留言提交失败');
  }
}

function handleAvatarError(e) {
  e.target.style.display = 'none';
  e.target.nextElementSibling.style.display = 'flex';
}

async function toggleComments(msg) {
  showComments.value[msg.messageId] = !showComments.value[msg.messageId];
  if (!commentForms.value[msg.messageId]) {
    commentForms.value[msg.messageId] = '';
  }
  if (showComments.value[msg.messageId] && (!msg.comments || msg.comments.length === 0)) {
    await loadComments(msg.messageId);
  }
}

async function loadComments(messageId) {
  try {
    const res = await getMessageComments(messageId);
    const msgIndex = messages.value.findIndex(m => m.messageId === messageId);
    if (msgIndex !== -1) {
      messages.value[msgIndex].comments = res.data || [];
    }
  } catch (e) {
    console.error('加载评论失败:', e);
  }
}

async function submitComment(messageId) {
  const content = commentForms.value[messageId];
  if (!content?.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  try {
    await createComment({ messageId, replyContent: content });
    ElMessage.success('评论成功');
    commentForms.value[messageId] = '';
    await loadComments(messageId);
    const msgIndex = messages.value.findIndex(m => m.messageId === messageId);
    if (msgIndex !== -1) {
      const currentCount = messages.value[msgIndex].commentCount || 0;
      messages.value[msgIndex].commentCount = currentCount + 1;
    }
  } catch (e) {
    ElMessage.error('评论失败');
  }
}

onMounted(async () => {
  await Promise.all([loadMessages(), loadShops(), loadGoods()]);
});
</script>

<style scoped lang="less">
.message-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;

  h2 {
    margin: 0;
    font-size: 22px;
    color: #303133;
  }
}

.message-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.submit-section {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
    color: #303133;
  }
}

.form-field {
  margin-bottom: 16px;
  position: relative;
}

.field-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.tip-text {
  background: #e8f4fd;
  color: #1677ff;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
  margin-bottom: 16px;
  border-left: 3px solid #1677ff;
}

.message-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
    font-weight: 500;
  }

  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    border: 1px solid #dcdfe6;
    transition: all 0.3s;

    &:focus {
      border-color: #165dff;
      box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
    }
  }
}

.suggestion-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  margin-top: 4px;
  max-height: 200px;
  overflow-y: auto;

  .suggestion-item {
    padding: 10px 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: background 0.2s;

    &:hover {
      background: #f5f7fa;
    }

    .suggestion-prefix {
      color: #165dff;
      margin-right: 6px;
      font-weight: 600;
    }

    &.developer-item {
      border-bottom: 1px solid #e4e7ed;
      padding-bottom: 10px;
      margin-bottom: 2px;

      .developer-name {
        color: rgb(22, 93, 255);
        font-weight: 600;
      }
    }
  }
}

.guest-tip {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;

  h3 {
    margin: 0;
    font-size: 16px;
    color: #303133;
  }
}

.sort-bar {
  flex-shrink: 0;

  :deep(.el-radio-group) {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  :deep(.el-radio-button) {
    padding: 6px 16px;
    background: #f5f7fa;
    border-radius: 20px;
    font-size: 14px;
    border: 1px solid #e4e7ed;
    color: #666;
    transition: all 0.2s;
    box-shadow: none;
    cursor: pointer;
    outline: none;
    -webkit-text-stroke: none;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 32px;
    margin-right: 0 !important;

    &.is-active {
      background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
      color: #fff;
      border-color: #165dff;
      text-shadow: none !important;
      -webkit-text-stroke: 0 !important;
      outline: none !important;

      .el-radio-button__inner {
        background: transparent !important;
        color: #fff !important;
        border: none !important;
        text-shadow: none !important;
        box-shadow: none !important;
        outline: none !important;
        -webkit-text-stroke: 0 !important;
        -webkit-text-fill-color: #fff !important;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        line-height: 1;
      }
    }

    &:hover:not(.is-active) {
      background: #e4e7ed;
    }

    &:active:not(.is-active) {
      background: #dcdfe6;
    }
  }

  :deep(.el-radio-button__inner) {
    border: none;
    background: transparent !important;
    padding: 0;
    font-size: 14px;
    box-shadow: none;
    color: inherit;
    outline: none;
    -webkit-text-stroke: none;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    line-height: 1;
  }

  :deep(.el-radio-button__orig-radio:checked+.el-radio-button__inner) {
    background: transparent !important;
    box-shadow: none;
    outline: none;
    -webkit-text-stroke: none;
  }

  :deep(.el-radio-button__inner:hover) {
    background: transparent !important;
  }

  :deep(.el-radio-button__inner:active) {
    background: transparent !important;
  }

  :deep(.el-radio-button__text) {
    text-shadow: none !important;
    -webkit-text-stroke: 0 !important;
    -webkit-text-fill-color: inherit !important;
    text-align: center !important;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.message-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.message-header {
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.message-body {
  margin-bottom: 14px;
}

.message-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.5;
}

.tag-shop {
  color: #165dff;
  text-decoration: underline;
  cursor: pointer;

  &:hover {
    color: #722ed1;
  }
}

.tag-goods {
  color: #f56c6c;
  text-decoration: underline;
  cursor: pointer;

  &:hover {
    color: #ef4444;
  }
}

.message-content-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.reply-count {
  font-size: 13px;
  color: #909399;
}

.comment-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  color: #165dff;
  font-size: 13px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.2s;

  &:hover {
    background: rgba(22, 93, 255, 0.1);
  }
}

.comments-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

.comment-input-area {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;

  :deep(.el-input) {
    flex: 1;
  }

  :deep(.el-input__inner) {
    border-radius: 20px;
    padding: 10px 16px;
  }
}

.send-comment-btn {
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: #fff;
  border: none;
  padding: 0 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    opacity: 0.9;
  }
}

.guest-comment-tip {
  text-align: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: #f9fafc;
  border-radius: 8px;

  &.is-top {
    background: linear-gradient(90deg, rgba(22, 93, 255, 0.06) 0%, rgba(114, 46, 209, 0.06) 100%);
    border: 1px solid rgba(22, 93, 255, 0.15);
  }

  &.is-merchant {
    background: linear-gradient(90deg, rgba(103, 194, 58, 0.06) 0%, rgba(133, 206, 97, 0.06) 100%);
    border: 1px solid rgba(103, 194, 58, 0.15);
  }
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;

  &.merchant-avatar {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-user {
  font-size: 13px;
  font-weight: 600;
  color: #303133;

  &.admin-user {
    color: #165dff;
  }

  &.merchant-user {
    color: #67c23a;
  }
}

.top-tag {
  margin: 0;
  line-height: 1;
}

.comment-time {
  font-size: 11px;
  color: #c0c4cc;
}

.comment-content p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  word-break: break-word;
}

.no-comments {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.gradient-btn {
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: white;
  border: none;
  padding: 10px 28px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
}

.apply-btn {
  height: 40px;
  padding: 0 32px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
}

.list-fade-enter-active,
.list-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 1000px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
<template>
  <div class="user-layout">
    <div class="user-sidebar">
      <div class="sidebar-header">
        <div class="avatar-wrapper">
          <div class="avatar-inner" @click="showAvatarUpload = true">
            <img v-if="userStore.userInfo.avatar" :src="userStore.userInfo.avatar" class="avatar-img" />
            <div v-else class="avatar">{{ userStore.userInfo.account?.charAt(0)?.toUpperCase() || 'U' }}</div>
            <div class="avatar-edit-tip">点击更换</div>
          </div>
        </div>
        <div class="user-name">{{ userStore.userInfo.account }}</div>
      </div>
      <el-menu :default-active="tab" @select="handleMenuSelect" class="sidebar-menu">
        <el-menu-item index="cart"><el-icon><ShoppingCart /></el-icon>购物车</el-menu-item>
        <el-menu-item index="orders"><el-icon><Document /></el-icon>我的订单</el-menu-item>
        <el-menu-item index="address"><el-icon><Location /></el-icon>收货地址</el-menu-item>
        <el-menu-item index="message"><el-icon><ChatDotRound /></el-icon>留言反馈</el-menu-item>
        <el-menu-item index="comment"><el-icon><ChatDotRound /></el-icon>我的评论</el-menu-item>
        <el-menu-item index="afterSale"><el-icon><Service /></el-icon>售后申请</el-menu-item>
        <el-menu-item index="merchant-apply"><el-icon><Shop /></el-icon>商家入驻</el-menu-item>
        <el-menu-item index="profile"><el-icon><User /></el-icon>个人资料</el-menu-item>
        <el-menu-item index="password"><el-icon><Lock /></el-icon>修改密码</el-menu-item>
        <el-menu-item index="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-menu-item>
      </el-menu>
    </div>
    <div class="user-main animate-fade-in-up">
      <div v-if="tab === 'profile'" class="section-card animate-fade-in">
        <h3>个人资料</h3>
        <el-form :model="profile" label-width="92px" style="max-width: 520px; margin: 0 auto">
          <el-form-item label="真实姓名" required><el-input v-model="profile.realName" /></el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profile.gender"><el-radio :value="0">保密</el-radio><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio></el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" required><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item label="邮箱" required><el-input v-model="profile.email" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="profile.address" /></el-form-item>
          <el-form-item><button type="button" class="gradient-btn apply-btn" @click="saveProfile">保存资料</button></el-form-item>
        </el-form>
      </div>

      <div v-if="tab === 'address'" class="section-card animate-fade-in">
        <div class="card-header-row">
          <h3>收货地址</h3>
          <el-button type="primary" class="apply-btn" @click="newAddress">新增地址</el-button>
        </div>
        <el-table v-loading="addressLoading" :data="addresses" border stripe style="width: 100%" class="large-table">
          <el-table-column prop="receiverName" label="收货人" width="150" />
          <el-table-column prop="receiverPhone" label="手机号" width="150" />
          <el-table-column prop="receiverAddress" label="地址" min-width="300" />
          <el-table-column prop="isDefault" label="默认" width="150">
            <template #default="{ row }"><el-tag :type="row.isDefault === 1 ? 'success' : 'info'" size="large">{{ row.isDefault === 1 ? '是' : '否' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link @click="editAddress(row)">编辑</el-button>
              <el-button link type="danger" @click="removeAddress(row.addressId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'message'" class="section-card animate-fade-in">
        <h3>留言反馈</h3>
        <el-table v-loading="messageLoading" :data="messages" border stripe style="width: 100%" class="large-table">
          <el-table-column prop="messageTitle" label="标题" width="200" />
          <el-table-column prop="messageContent" label="内容" min-width="250" />
          <el-table-column label="发布时间" width="180">
            <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
          </el-table-column>
          <el-table-column prop="replyContent" label="回复" min-width="100">
            <template #default="{ row }"><span v-if="row.replyContent">{{ row.replyContent }}</span><el-tag v-else type="warning" size="small">未回复</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link @click="editMessage(row)">编辑</el-button>
              <el-button link type="danger" @click="removeMessage(row.messageId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'comment'" class="section-card animate-fade-in">
        <h3>我的评论</h3>
        <el-table v-loading="commentLoading" :data="comments" border stripe style="width: 100%" class="large-table">
          <el-table-column prop="messageTitle" label="留言标题" min-width="150" show-overflow-tooltip />
          <el-table-column prop="messageContent" label="留言内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="commentContent" label="评论内容" min-width="300" />
          <el-table-column label="发布时间" width="240">
            <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
          </el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="{ row }">
              <el-button link @click="editComment(row)">编辑</el-button>
              <el-button link type="danger" @click="removeComment(row.commentId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'afterSale'" class="section-card animate-fade-in">
        <h3>售后申请</h3>
        <el-table v-loading="afterSaleLoading" :data="afterSales" border stripe style="width: 100%" class="large-table">
          <el-table-column label="订单编号" width="210">
            <template #default="{ row }">
              <a class="order-link" @click.prevent="viewAfterSaleOrder(row.orderId)">{{ row.orderNo || getOrderNo(row.orderId) }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="refundAmount" label="退款金额" width="120">
            <template #default="{ row }">¥{{ row.refundAmount }}</template>
          </el-table-column>
          <el-table-column prop="auditStatus" label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="row.auditStatus === 0 ? 'warning' : row.auditStatus === 1 ? 'success' : 'danger'">
                {{ row.auditStatus === 0 ? (row.escalated === 1 ? '平台处理中' : '待审核') : row.auditStatus === 1 ? '已通过' : '已驳回' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="refundReason" label="退款原因" min-width="150" show-overflow-tooltip />
          <el-table-column label="凭证" width="130">
            <template #default="{ row }">
              <img v-if="row.refundVoucher" :src="row.refundVoucher" style="width:60px;height:60px;object-fit:cover;cursor:pointer;border-radius:4px" @click="openPreview(row.refundVoucher)" />
              <span v-else style="color:#999">无</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="200">
            <template #default="{ row }">
              <div v-if="row.auditRemark || row.adminRemark">
                <div v-if="row.auditRemark">商家：{{ row.auditRemark }}</div>
                <div v-if="row.adminRemark" style="margin-top:4px">开发者：{{ row.adminRemark }}</div>
              </div>
              <span v-else style="color:#999">无</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button v-if="row.auditStatus === 2 && row.escalated !== 1" size="small" type="warning" @click="escalateAfterSale(row.afterSaleId)">申请平台介入</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'password'" class="section-card">
        <h3>修改密码</h3>
        <el-form :model="pwdForm" label-width="100px" style="max-width: 460px; margin: 0 auto">
          <el-form-item label="原密码" required><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码" required><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认新密码" required><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
          <el-form-item><button type="button" class="gradient-btn apply-btn" @click="changePwd">确认修改</button></el-form-item>
        </el-form>
      </div>

      <div v-if="tab === 'cart'" class="section-card">
        <h3>购物车</h3>
        <el-table 
          v-loading="cartLoading" 
          :data="cartItems" 
          border 
          stripe 
          style="width: 100%" 
          class="large-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="80" />
          <el-table-column label="商品名称" min-width="200">
            <template #default="{ row }">
              <router-link class="goods-link" :to="'/goods/' + row.goodsId">{{ row.goodsName }}</router-link>
            </template>
          </el-table-column>
          <el-table-column prop="salePrice" label="单价" width="170" />
          <el-table-column label="数量" width="170">
            <template #default="{ row }">
              <el-input-number v-model="row.goodsNum" :min="1" size="small" @change="updateNum(row)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="170">
            <template #default="{ row }">
              <span class="price-text">¥{{ (row.salePrice * row.goodsNum).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeItem(row.cartId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="cart-footer">
          <div class="total">合计：<span class="gradient-text">¥{{ totalAmount }}</span></div>
          <button class="gradient-btn checkout-btn" @click="checkout">立即下单</button>
        </div>
      </div>

      <div v-if="tab === 'orders'" class="section-card">
        <h3>我的订单</h3>
        <el-table v-loading="ordersLoading" :data="orders" border stripe style="width: 100%" class="large-table">
          <el-table-column label="订单编号" width="210">
            <template #default="{ row }">
              <a class="order-link" @click.prevent="viewOrderDetail(row.orderId)">{{ row.orderNo }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总金额" width="100" />
          <el-table-column prop="orderStatus" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.orderStatus)">{{ statusMap[row.orderStatus] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="receiverName" label="收货人" width="120" />
          <el-table-column prop="receiverAddress" label="收货地址" min-width="200" />
          <el-table-column label="付款时间" width="140">
            <template #default="{ row }"><span v-html="formatDateTimeHtml(row.payTime)"></span></template>
          </el-table-column>
          <el-table-column label="完成时间" width="140">
            <template #default="{ row }"><span v-html="formatDateTimeHtml(row.receiveTime)"></span></template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="{ row }">
              <el-button v-if="row.orderStatus === 0" size="small" type="primary" @click="payOrder(row.orderId)">付款</el-button>
              <el-button v-if="row.orderStatus === 0" size="small" type="danger" @click="cancelOrder(row.orderId)">取消</el-button>
              <el-button v-if="row.orderStatus === 2 && !hasAfterSale(row.orderId)" size="small" type="warning" @click="openAfterSaleDialog(row)">申请售后</el-button>
              <el-button v-if="row.orderStatus === 2 && !hasAfterSale(row.orderId)" size="small" type="success" @click="confirmReceive(row.orderId)">确认收货</el-button>
              <el-button v-if="row.orderStatus === 3 || row.orderStatus === 4 || row.orderStatus === 6" size="small" type="danger" @click="deleteOrder(row.orderId)">删除记录</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'merchant-apply'" class="section-card animate-fade-in">
        <h3>商家入驻申请
        <el-alert v-if="merchantApplyStatus === 1" title="您已成为入驻商家" type="success" :closable="false" show-icon style="margin-bottom: 16px" center />
        <el-alert v-if="merchantApplyStatus === 0" title="您的入驻申请已提交，审核结束后，结果将发送到您的邮箱..." type="warning" :closable="false" show-icon style="margin-bottom: 16px" center />
        <el-alert v-if="merchantApplyStatus === 2" :title="'您的入驻申请未通过，请重新提交，驳回原因：' + (merchantApplyInfo.auditRemark || '无')" type="error" :closable="false" show-icon style="margin-bottom: 16px" center />
        </h3>
        <el-form v-loading="merchantLoading" :model="merchantForm" :rules="merchantRules" ref="merchantFormRef" label-width="110px" style="max-width: 520px; margin: 0 auto">
          <el-divider content-position="center">经营人信息（自动补全）</el-divider>
          <el-form-item label="经营人姓名"><el-input v-model="userInfo.realName" disabled placeholder="加载中..." /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="userInfo.phone" disabled placeholder="加载中..." /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="userInfo.email" disabled placeholder="加载中..." /></el-form-item>
          <el-form-item label="经营地址"><el-input v-model="userInfo.address" disabled placeholder="加载中..." /></el-form-item>
          
          <el-divider content-position="center">店铺信息</el-divider>
          <el-form-item label="店铺名称" prop="shopName"><el-input v-model="merchantForm.shopName" placeholder="店铺名称" :disabled="merchantApplyStatus === 1 || merchantApplyStatus === 0" /></el-form-item>
          <el-form-item label="店铺Logo" prop="shopLogo">
            <el-upload
              class="logo-uploader"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload"
              action="/api/upload/logo"
              accept=".png,.jpg,.jpeg"
              :disabled="merchantApplyStatus === 1 || merchantApplyStatus === 0"
            >
              <img v-if="merchantForm.shopLogo" :src="merchantForm.shopLogo" class="logo-preview" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="店铺描述"><el-input v-model="merchantForm.shopDesc" type="textarea" :rows="3" placeholder="店铺描述" :disabled="merchantApplyStatus === 1 || merchantApplyStatus === 0" /></el-form-item>
          <el-form-item label="营业时间" prop="businessHours">
            <el-select v-model="merchantForm.businessHours" placeholder="请选择营业时间" style="width: 100%" :disabled="merchantApplyStatus === 1 || merchantApplyStatus === 0">
              <el-option label="全天营业 (00:00-24:00)" value="00:00-24:00" />
              <el-option label="早班 (06:00-18:00)" value="06:00-18:00" />
              <el-option label="日班 (08:00-20:00)" value="08:00-20:00" />
              <el-option label="正常营业 (09:00-21:00)" value="09:00-21:00" />
              <el-option label="标准营业 (09:00-22:00)" value="09:00-22:00" />
              <el-option label="晚班 (10:00-23:00)" value="10:00-23:00" />
              <el-option label="夜间营业 (14:00-02:00)" value="14:00-02:00" />
            </el-select>
          </el-form-item>
          
          <el-divider content-position="center">商家账号设置</el-divider>
          <el-form-item label="商家账号"><el-input v-model="generatedAccount" disabled placeholder="系统自动分配" /></el-form-item>
          <el-form-item label="商家密码" prop="merchantPassword"><el-input v-model="merchantForm.merchantPassword" type="password" show-password placeholder="设置商家独立登录密码" :disabled="merchantApplyStatus === 1 || merchantApplyStatus === 0" /></el-form-item>
          
          <el-form-item>
            <button 
              type="button" 
              class="gradient-btn apply-btn" 
              @click="submitMerchantApply"
              :disabled="merchantApplyStatus === 1 || merchantApplyStatus === 0"
              :style="{ opacity: (merchantApplyStatus === 1 || merchantApplyStatus === 0) ? 0.5 : 1, cursor: (merchantApplyStatus === 1 || merchantApplyStatus === 0) ? 'not-allowed' : 'pointer' }"
            >提交入驻申请</button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-dialog v-model="showAddressDialog" title="地址维护" width="450px">
      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收货人" required><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="手机号" required><el-input v-model="addressForm.receiverPhone" /></el-form-item>
        <el-form-item label="地址" required><el-input v-model="addressForm.receiverAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddressAction">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAvatarUpload" title="更换头像" width="400px" center>
      <div class="avatar-upload-content">
        <ImageUpload
          v-model="tempAvatar"
          upload-url="/api/upload/avatar"
          tip="支持JPG/PNG/WEBP格式，不超过2MB"
          @success="handleAvatarSuccess"
        />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: center; gap: 12px">
          <el-button @click="showAvatarUpload = false">取消</el-button>
          <el-button type="primary" @click="saveAvatar">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="showMessageDialog" title="留言编辑" width="450px">
      <el-form :model="messageForm" label-width="90px">
        <el-form-item label="标题" required><el-input v-model="messageForm.messageTitle" /></el-form-item>
        <el-form-item label="内容" required><el-input v-model="messageForm.messageContent" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMessageDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMessageAction">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCommentDialog" title="评论编辑" width="450px" >
      <el-form :model="commentForm" label-width="90px">
        <el-form-item label="评论内容" required><el-input v-model="commentForm.commentContent" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCommentDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCommentAction">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="orderDetailVisible" title="订单详情" width="650px">
      <template v-if="orderDetailData">
        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="订单编号">{{ orderDetailData.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ statusMap[orderDetailData.order.orderStatus] }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ orderDetailData.order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetailData.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetailData.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ orderDetailData.order.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 12px 0 8px">订单商品</h4>
        <el-table :data="orderDetailData.items" border size="small">
          <el-table-column prop="goodsName" label="商品名称" min-width="150" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ row.goodsPrice }}</template>
          </el-table-column>
          <el-table-column prop="goodsNum" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ row.totalPrice }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <el-dialog v-model="afterSaleDialogVisible" title="申请售后" width="500px">
      <el-form :model="afterSaleForm" label-width="90px">
        <el-form-item label="退款原因" required>
          <el-input v-model="afterSaleForm.refundReason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
        <el-form-item label="上传凭证">
          <el-upload
            class="voucher-uploader"
            :show-file-list="false"
            :on-success="handleVoucherSuccess"
            :before-upload="beforeVoucherUpload"
            action="/api/upload/avatar"
            accept=".png,.jpg,.jpeg"
          >
            <img v-if="afterSaleForm.refundVoucher" :src="afterSaleForm.refundVoucher" class="voucher-img" />
            <div v-else class="voucher-placeholder">
              <el-icon class="voucher-icon"><Plus /></el-icon>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="afterSaleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAfterSale">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" width="50%" :show-close="true" @close="previewVisible = false; previewImageUrl = ''" destroy-on-close>
      <div style="display:flex;justify-content:center;align-items:center;min-height:50vh">
        <img :src="previewImageUrl" style="max-width:100%;max-height:50vh;object-fit:contain" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { User, Location, ChatDotRound, Service, Lock, ShoppingCart, Document, Shop, SwitchButton, Plus } from '@element-plus/icons-vue';
import { createMessage, deleteAddress, deleteMessage, getAddressList, getAfterSaleList, applyAfterSale, escalateAfterSale as escalateAfterSaleApi, getMessageList, getProfile, saveAddress, updateProfile, changeUserPassword, getCartList, updateCartNum, deleteCartItem, createOrder, getOrderList, payOrderApi, confirmReceiveOrder, applyRefundOrder, cancelOrderApi, deleteOrderApi, getOrderDetail, applyMerchant, getMerchantProgress, updateMessage, getCommentList, deleteComment } from '../../api/user';
import { useUserStore } from '../../store/user';
import ImageUpload from '../../components/ImageUpload.vue';
import { formatDateTimeHtml } from '../../utils/datetime';

const router = useRouter();
const userStore = useUserStore();
userStore.syncFromStorage();
const tab = ref('cart');
const profile = reactive({ realName: '', phone: '', email: '', address: '', gender: 0, avatar: '', userAvatar: '' });
const userInfo = reactive({ realName: '', phone: '', email: '', address: '', isMerchant: 0 });
const addresses = ref([]);
const messages = ref([]);
const comments = ref([]);
const afterSales = ref([]);
const msgForm = reactive({ messageTitle: '', messageContent: '' });
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' });
const showAddressDialog = ref(false);
const addressForm = reactive({ addressId: null, receiverName: '', receiverPhone: '', receiverAddress: '', isDefault: 0 });
const showAvatarUpload = ref(false);
const tempAvatar = ref('');
const previewVisible = ref(false);
const previewImageUrl = ref('');

function openPreview(url) {
  previewImageUrl.value = url;
  previewVisible.value = true;
}
const showMessageDialog = ref(false);
const messageForm = reactive({ messageId: null, messageTitle: '', messageContent: '' });
const showCommentDialog = ref(false);
const commentForm = reactive({ commentId: null, commentContent: '' });

const cartItems = ref([]);
const cartLoading = ref(false);
const selectedCartItems = ref([]);
const addressLoading = ref(false);
const messageLoading = ref(false);
const commentLoading = ref(false);
const afterSaleLoading = ref(false);
const totalAmount = computed(() => {
  const itemsToCalculate = selectedCartItems.value.length > 0 ? selectedCartItems.value : cartItems.value;
  return itemsToCalculate.reduce((s, i) => s + Number(i.salePrice) * Number(i.goodsNum), 0).toFixed(2);
});

const orders = ref([]);
const ordersLoading = ref(false);
const statusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '售后中', 6: '已退款' };
function statusType(s) { return { 0: 'warning', 1: '', 2: '', 3: 'success', 4: 'info', 5: 'danger', 6: '' }[s] || ''; }

const orderDetailVisible = ref(false);
const orderDetailData = ref(null);

const afterSaleDialogVisible = ref(false);
const afterSaleForm = reactive({ orderId: null, itemId: null, refundAmount: '', refundReason: '', refundVoucher: '' });
const afterSaleOrder = ref(null);

const merchantFormRef = ref();
const merchantForm = reactive({ shopName: '', shopLogo: '', shopDesc: '', businessHours: '', merchantPassword: '' });
const merchantRules = {
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  businessHours: [{ required: true, message: '请选择营业时间', trigger: 'change' }],
  merchantPassword: [{ required: true, message: '请输入商家密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码6-20位', trigger: 'blur' }]
};
const generatedAccount = ref('系统自动分配');
const merchantLoading = ref(false);
const merchantApplyStatus = ref(-1);
const merchantApplyInfo = ref({});

function logout() { userStore.clearAuth(); router.push('/login'); }

function handleMenuSelect(index) {
  if (index === 'logout') {
    logout();
  } else {
    tab.value = index;
    if (index === 'cart') { loadCart(); loadAddresses(); }
    if (index === 'orders') loadOrders();
    if (index === 'message') loadMessages();
    if (index === 'comment') loadComments();
    if (index === 'merchant-apply') loadMerchantInfo();
  }
}

async function loadAll() {
  try {
    const profileRes = await getProfile();
    if (profileRes && profileRes.data) {
      Object.assign(profile, profileRes.data);
      profile.avatar = profileRes.data.userAvatar || '';
      userStore.userInfo.avatar = profileRes.data.userAvatar || '';
      Object.assign(userInfo, profileRes.data);
    }
  } catch (e) { console.error('加载个人资料失败:', e); }

  try { addresses.value = (await getAddressList()).data || []; } catch (e) { console.error('加载地址失败:', e); }
  try { messages.value = (await getMessageList()).data || []; } catch (e) { console.error('加载留言失败:', e); }
  try { afterSales.value = (await getAfterSaleList()).data || []; } catch (e) { console.error('加载售后失败:', e); }
  try { comments.value = (await getCommentList()).data || []; } catch (e) { console.error('加载评论失败:', e); }

  await loadCart();
}

async function saveProfile() { 
  await updateProfile(profile); 
  ElMessage.success('资料已保存'); 
  try {
    const profileRes = await getProfile();
    if (profileRes && profileRes.data) {
      Object.assign(profile, profileRes.data);
      profile.avatar = profileRes.data.userAvatar || '';
      Object.assign(userInfo, profileRes.data);
    }
  } catch (e) { console.error('刷新资料失败', e); }
}

function newAddress() { Object.assign(addressForm, { addressId: null, receiverName: '', receiverPhone: '', receiverAddress: '', isDefault: 0 }); showAddressDialog.value = true; }
function editAddress(row) { Object.assign(addressForm, row); showAddressDialog.value = true; }
async function saveAddressAction() { await saveAddress(addressForm); showAddressDialog.value = false; await loadAll(); }
async function removeAddress(addressId) { await deleteAddress(addressId); await loadAll(); }
async function loadAddresses() {
  addressLoading.value = true;
  try {
    const res = await getAddressList();
    addresses.value = res.data || [];
  } catch (error) {
    console.error('加载地址失败:', error);
  } finally {
    addressLoading.value = false;
  }
}
async function submitMessage() { await createMessage(msgForm); Object.assign(msgForm, { messageTitle: '', messageContent: '' }); await loadAll(); }
function editMessage(row) { Object.assign(messageForm, { messageId: row.messageId, messageTitle: row.messageTitle, messageContent: row.messageContent }); showMessageDialog.value = true; }
async function saveMessageAction() { await updateMessage(messageForm); showMessageDialog.value = false; await loadAll(); ElMessage.success('留言修改成功'); }
function editComment(row) { Object.assign(commentForm, { commentId: row.commentId, commentContent: row.commentContent }); showCommentDialog.value = true; }
async function saveCommentAction() { showCommentDialog.value = false; await loadComments(); ElMessage.success('评论修改成功'); }
async function removeMessage(messageId) { await deleteMessage(messageId); await loadAll(); }

async function loadMessages() {
  messageLoading.value = true;
  try {
    const res = await getMessageList();
    messages.value = res.data || [];
  } catch (error) {
    console.error('加载留言失败:', error);
  } finally {
    messageLoading.value = false;
  }
}

async function loadComments() {
  commentLoading.value = true;
  try {
    const res = await getCommentList();
    comments.value = res.data || [];
  } catch (error) {
    console.error('加载评论失败:', error);
  } finally {
    commentLoading.value = false;
  }
}

async function removeComment(commentId) {
  await deleteComment(commentId);
  await loadComments();
}

async function changePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) { ElMessage.warning('请填写完整'); return; }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return; }
  await changeUserPassword(pwdForm);
  ElMessage.success('密码修改成功');
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' });
}

async function loadCart() {
  cartLoading.value = true;
  try {
    const res = await getCartList();
    cartItems.value = res.data || [];
    selectedCartItems.value = [];
  } catch (error) {
    console.error('加载购物车失败:', error);
    ElMessage.error('加载购物车失败');
  } finally {
    cartLoading.value = false;
  }
}

function handleSelectionChange(selection) {
  selectedCartItems.value = selection;
}

async function updateNum(row) {
  try {
    await updateCartNum({ cartId: row.cartId, goodsNum: row.goodsNum });
  } catch (e) {
    ElMessage.error('更新数量失败');
    await loadCart();
  }
}

async function removeItem(cartId) { await deleteCartItem(cartId); await loadCart(); }

async function checkout() {
  const itemsToCheckout = selectedCartItems.value.length > 0 ? selectedCartItems.value : cartItems.value;
  if (!itemsToCheckout.length) { ElMessage.warning('购物车为空'); return; }
  if (!addresses.value.length) {
    ElMessage.warning('请先完善收货地址');
    tab.value = 'address';
    return;
  }
  if (!userInfo.realName || !userInfo.phone || !userInfo.email) {
    ElMessage.warning('请先完善个人信息（真实姓名、手机号、邮箱）');
    tab.value = 'profile';
    return;
  }
  
  const defaultAddress = addresses.value.find(a => a.isDefault === 1) || addresses.value[0];
  const cartIds = itemsToCheckout.map(item => item.cartId);
  
  try {
    await createOrder({ cartIds, addressId: defaultAddress.addressId, payType: 'alipay' });
    ElMessage.success('下单成功');
    tab.value = 'orders';
    loadOrders();
    loadCart();
  } catch (error) {
    console.error('下单失败:', error);
    ElMessage.error(error.message || '下单失败，请重试');
  }
}

async function loadOrders() {
  ordersLoading.value = true;
  try {
    const res = await getOrderList();
    if (res && res.data) {
      orders.value = res.data.records || res.data || [];
    } else {
      orders.value = [];
    }
  } catch (error) {
    console.error('加载订单失败:', error);
    ElMessage.error('加载订单失败: ' + (error.message || '请稍后重试'));
    orders.value = [];
  } finally {
    ordersLoading.value = false;
  }
}
async function payOrder(orderId) { try { await payOrderApi(orderId); ElMessage.success('支付成功'); loadOrders(); } catch (e) { ElMessage.error('支付失败'); } }
async function confirmReceive(orderId) { try { await confirmReceiveOrder(orderId); ElMessage.success('已确认收货'); loadOrders(); } catch (e) { ElMessage.error('确认收货失败'); } }
async function applyRefund(orderId) { try { await applyRefundOrder(orderId); ElMessage.success('退款申请已提交'); loadOrders(); } catch (e) { ElMessage.error('申请退款失败'); } }
async function cancelOrder(orderId) { try { await cancelOrderApi(orderId); ElMessage.success('订单已取消'); loadOrders(); } catch (e) { ElMessage.error('取消订单失败'); } }
async function deleteOrder(orderId) {
  try {
    await ElMessageBox.confirm('确定删除该订单吗？删除后不可恢复。', '提示', { type: 'warning' });
    await deleteOrderApi(orderId);
    ElMessage.success('购买记录已删除');
    loadOrders();
  } catch (e) {
    if (e !== 'cancel') { ElMessage.error('删除购买记录失败'); }
  }
}
async function viewOrderDetail(orderId) {
  try {
    const res = await getOrderDetail(orderId);
    orderDetailData.value = res.data;
    orderDetailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取订单详情失败');
  }
}

function getOrderNo(orderId) {
  const order = orders.value.find(o => o.orderId === orderId);
  return order ? order.orderNo : orderId;
}

async function viewAfterSaleOrder(orderId) {
  try {
    const res = await getOrderDetail(orderId);
    orderDetailData.value = res.data;
    orderDetailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取订单详情失败');
  }
}

async function openAfterSaleDialog(row) {
  afterSaleOrder.value = row;
  afterSaleForm.orderId = row.orderId;
  afterSaleForm.refundAmount = row.totalAmount;
  afterSaleForm.refundReason = '';
  afterSaleForm.refundVoucher = '';
  afterSaleForm.itemId = null;
  try {
    const res = await getOrderDetail(row.orderId);
    if (res.data && res.data.items && res.data.items.length > 0) {
      afterSaleForm.itemId = res.data.items[0].itemId;
    }
  } catch (e) {}
  afterSaleDialogVisible.value = true;
}

function handleVoucherSuccess(response) {
  if (response.code === 200) {
    afterSaleForm.refundVoucher = response.data;
  } else {
    ElMessage.error(response.message || '凭证上传失败');
  }
}

function beforeVoucherUpload(file) {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg';
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) { ElMessage.error('只能上传 JPG/PNG 格式的图片!'); return false; }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB!'); return false; }
  return true;
}

async function submitAfterSale() {
  if (!afterSaleForm.refundReason.trim()) {
    ElMessage.warning('请填写退款原因');
    return;
  }
  try {
    await applyAfterSale(afterSaleForm);
    ElMessage.success('售后申请已提交');
    afterSaleDialogVisible.value = false;
    afterSales.value = (await getAfterSaleList()).data || [];
    tab.value = 'afterSale';
    loadOrders();
  } catch (e) {
    ElMessage.error('提交售后申请失败');
  }
}

function hasAfterSale(orderId) {
  return afterSales.value.some(a => a.orderId === orderId);
}

async function escalateAfterSale(afterSaleId) {
  try {
    await ElMessageBox.confirm('确定申请平台介入处理吗？', '提示', { type: 'warning' });
  } catch (e) {
    return;
  }
  try {
    await escalateAfterSaleApi(afterSaleId);
    ElMessage.success('已申请平台介入');
    afterSales.value = (await getAfterSaleList()).data || [];
  } catch (e) {
    if (e && e.message) {
      ElMessage.error(e.message);
    }
  }
}

async function loadMerchantInfo() {
  merchantLoading.value = true;
  try {
    if (!userInfo.realName || !userInfo.phone || !userInfo.email) {
      ElMessage.warning('请先完善个人信息（真实姓名、手机号、邮箱）');
      tab.value = 'profile';
      return;
    }
    const res = await getMerchantProgress();
    if (res && res.data) {
      merchantApplyInfo.value = res.data;
      merchantApplyStatus.value = res.data.auditStatus;
      if (res.data.isMerchant === 1) {
        merchantApplyStatus.value = 1;
      }
      if (res.data.shopName) merchantForm.shopName = res.data.shopName;
      if (res.data.shopLogo) merchantForm.shopLogo = res.data.shopLogo;
      if (res.data.shopDesc) merchantForm.shopDesc = res.data.shopDesc;
      if (res.data.businessHours) merchantForm.businessHours = res.data.businessHours;
      if (res.data.merchantAccount) generatedAccount.value = res.data.merchantAccount;
    }
  } catch (error) {
    console.error('获取入驻信息失败:', error);
    ElMessage.error('获取入驻信息失败，请稍后重试');
  } finally {
    merchantLoading.value = false;
  }
}
function beforeLogoUpload(file) {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg';
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
}
function handleLogoSuccess(response) {
  if (response.code === 200) {
    merchantForm.shopLogo = response.data;
    ElMessage.success('Logo上传成功');
  } else {
    ElMessage.error(response.message || 'Logo上传失败');
  }
}
async function submitMerchantApply() {
  await merchantFormRef.value.validate();
  await applyMerchant(merchantForm);
  ElMessage.success('入驻申请已提交，请等待审核');
  loadMerchantInfo();
}

function handleAvatarSuccess(url) {
  tempAvatar.value = url;
}
async function saveAvatar() {
  if (!tempAvatar.value) {
    ElMessage.warning('请先上传头像');
    return;
  }
  try {
    const res = await updateProfile({ userAvatar: tempAvatar.value });
    userStore.updateAvatar(tempAvatar.value);
    const p = await getProfile();
    if (p && p.data) {
      Object.assign(profile, p.data);
      userStore.userInfo.avatar = p.data.userAvatar || tempAvatar.value;
    }
    ElMessage.success('头像更新成功');
    showAvatarUpload.value = false;
    tempAvatar.value = '';
  } catch (error) {
    console.error('头像保存失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '头像保存失败，请重试');
  }
}

onMounted(loadAll);
</script>

<style scoped lang="less">
.user-layout {
  display: flex;
  min-height: 100vh;
  background: rgb(255, 255, 255);
  position: relative;
  overflow: hidden;
}

.user-sidebar {
  width: 220px;
  background: linear-gradient(180deg, #232552 0%, #2d2e5e 100%);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 0;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  padding: 24px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  .avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: var(--theme-primary-gradient);
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 10px;
  }

  .user-name {
    color: #fff;
    font-size: 14px;
  }
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  flex: 1;

  :deep(.el-menu-item) {
    color: #a0a3bd;
  }

  :deep(.el-menu-item.is-active) {
    background: var(--theme-primary-gradient) !important;
    color: #fff !important;
  }

  :deep(.el-menu-item:hover) {
    background: rgba(22, 93, 255, 0.15) !important;
  }
}

.user-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  margin-left: 220px;
}

.section-card {
  background: #ffffff;
  border-radius: var(--theme-radius-lg);
  padding: 32px;
  box-shadow: var(--theme-shadow);
  font-size: 15px;
  line-height: 1.6;

  h3 {
    font-size: 20px;
    margin: 0 0 24px;
    color: var(--theme-text);
    font-weight: 600;
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
    align-items: center;
  }

  :deep(.el-form-item__label) {
    font-size: 15px;
    font-weight: 500;
    display: flex;
    align-items: center;
    height: 44px;
    line-height: 44px;
  }

  :deep(.el-form-item__content) {
    display: flex;
    align-items: center;
  }

  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    font-size: 15px;
    padding: 12px 14px;
    height: 44px;
    line-height: 1.5;
  }

  :deep(.el-textarea__inner) {
    height: auto;
    min-height: 100px;
  }

  :deep(.el-select) {
    width: 100% !important;

    .el-select__wrapper {
      width: 100%;
      height: 44px;
      padding: 0 14px;
      box-sizing: border-box;
    }

    .el-select__placeholder,
    .el-select__value {
      line-height: 44px;
      font-size: 15px;
    }
  }
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 { margin: 0; }
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 8px;

  .total {
    font-size: 18px;
    font-weight: 600;
  }

  .checkout-btn {
    height: 40px;
    padding: 0 32px;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
  }
}

.goods-link {
  color: #165dff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;

  &:hover {
    color: #722ed1;
    text-decoration: underline;
  }
}

.order-link {
  color: #165dff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;

  &:hover {
    color: #722ed1;
    text-decoration: underline;
  }
}

.logo-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--theme-border);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--theme-transition);

    &:hover {
      border-color: var(--theme-primary);
    }
  }
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  line-height: 148px;
  text-align: center;
}

.logo-preview {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.apply-btn {
  height: 40px;
  padding: 0 32px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.voucher-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #409eff;
    }
  }
}

.voucher-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.voucher-icon {
  font-size: 28px;
  color: #8c939d;
}

.voucher-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.avatar-wrapper {
  position: relative;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-inner {
    position: relative;
    cursor: pointer;
    width: 56px;
    height: 56px;
  }

  .avatar, .avatar-img {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: block;
    object-fit: cover;
  }

  .avatar {
    background: var(--theme-primary-gradient);
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .avatar-edit-tip {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 10px;
    text-align: center;
    padding: 2px 0;
    border-radius: 0 0 28px 28px;
    opacity: 0;
    transition: opacity 0.2s;
  }

  .avatar-inner:hover .avatar-edit-tip {
    opacity: 1;
  }
}

.large-table {
  width: 100%;
  overflow-x: auto;

  :deep(.el-table) {
    width: 100%;
    min-width: 100%;
  }

  :deep(.el-table__cell) {
    font-size: 15px;
    padding: 16px 12px;
    line-height: 1.6;
    white-space: normal;
    word-break: break-all;
    text-align: center;
    vertical-align: middle;
  }

  :deep(.el-table__header th) {
    font-size: 15px;
    font-weight: 600;
    padding: 16px 12px;
    line-height: 1.6;
    white-space: normal;
    word-break: break-all;
    text-align: center;
    vertical-align: middle;
  }

  :deep(.el-table__row) {
    height: auto;
  }

  :deep(.el-table--scrollable-x .el-table__body-wrapper) {
    overflow-x: auto;
  }

  :deep(.el-checkbox) {
    transform: scale(1.5);
    margin: 0;
  }

  :deep(.el-checkbox__input) {
    &::before {
      border-color: #333 !important;
      border-width: 2px !important;
    }
  }
}

.section-card :deep(.el-divider__text) {
  font-weight: 600;
  font-size: 15px;
}

.animate-fade-in {
  animation: fade-in 0.3s ease-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
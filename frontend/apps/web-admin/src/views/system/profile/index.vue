<script lang="ts" setup>
import { ref } from 'vue';

import { useUserStore } from '@vben/stores';

import {
  Button,
  Card,
  Col,
  Form,
  FormItem,
  Input,
  message,
  Row,
  Tabs,
  TabPane,
} from 'ant-design-vue';

import { updatePassword, updateProfile } from '#/api/system/user';

defineOptions({ name: 'SystemProfile' });

const userStore = useUserStore();
const userInfo = userStore.userInfo as Record<string, any>;

const profileForm = ref({
  phone: userInfo?.phone ?? '',
  realName: userInfo?.realName ?? '',
});

const passwordForm = ref({
  confirmPassword: '',
  newPassword: '',
  oldPassword: '',
});

const profileLoading = ref(false);
const passwordLoading = ref(false);

async function handleSaveProfile() {
  profileLoading.value = true;
  try {
    await updateProfile(profileForm.value);
    message.success('个人信息更新成功');
  } finally {
    profileLoading.value = false;
  }
}

async function handleChangePassword() {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    message.error('两次输入的新密码不一致');
    return;
  }
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    message.error('请填写完整密码信息');
    return;
  }
  passwordLoading.value = true;
  try {
    await updatePassword(
      passwordForm.value.oldPassword,
      passwordForm.value.newPassword,
    );
    message.success('密码修改成功');
    passwordForm.value = { confirmPassword: '', newPassword: '', oldPassword: '' };
  } finally {
    passwordLoading.value = false;
  }
}
</script>

<template>
  <div class="p-4">
    <Row :gutter="16">
      <Col :span="8">
        <Card title="个人信息">
          <div class="flex flex-col items-center pb-4">
            <div
              class="bg-primary/10 text-primary mb-2 flex h-16 w-16 items-center justify-center rounded-full text-2xl font-bold"
            >
              {{ userInfo?.realName?.charAt(0) ?? 'U' }}
            </div>
            <div class="text-lg font-medium">{{ userInfo?.realName }}</div>
            <div class="text-gray-400">{{ userInfo?.username }}</div>
          </div>
        </Card>
      </Col>
      <Col :span="16">
        <Card>
          <Tabs>
            <TabPane key="profile" tab="基本资料">
              <Form
                :label-col="{ span: 4 }"
                :wrapper-col="{ span: 16 }"
                class="mt-4"
              >
                <FormItem label="用户名">
                  <Input :value="userInfo?.username" disabled />
                </FormItem>
                <FormItem label="姓名">
                  <Input
                    v-model:value="profileForm.realName"
                    placeholder="请输入姓名"
                  />
                </FormItem>
                <FormItem label="手机号">
                  <Input
                    v-model:value="profileForm.phone"
                    placeholder="请输入手机号"
                  />
                </FormItem>
                <FormItem :wrapper-col="{ offset: 4 }">
                  <Button
                    :loading="profileLoading"
                    type="primary"
                    @click="handleSaveProfile"
                  >
                    保存修改
                  </Button>
                </FormItem>
              </Form>
            </TabPane>
            <TabPane key="password" tab="修改密码">
              <Form
                :label-col="{ span: 4 }"
                :wrapper-col="{ span: 16 }"
                class="mt-4"
              >
                <FormItem label="当前密码" required>
                  <Input.Password
                    v-model:value="passwordForm.oldPassword"
                    placeholder="请输入当前密码"
                  />
                </FormItem>
                <FormItem label="新密码" required>
                  <Input.Password
                    v-model:value="passwordForm.newPassword"
                    placeholder="请输入新密码"
                  />
                </FormItem>
                <FormItem label="确认密码" required>
                  <Input.Password
                    v-model:value="passwordForm.confirmPassword"
                    placeholder="请再次输入新密码"
                  />
                </FormItem>
                <FormItem :wrapper-col="{ offset: 4 }">
                  <Button
                    :loading="passwordLoading"
                    type="primary"
                    @click="handleChangePassword"
                  >
                    修改密码
                  </Button>
                </FormItem>
              </Form>
            </TabPane>
          </Tabs>
        </Card>
      </Col>
    </Row>
  </div>
</template>

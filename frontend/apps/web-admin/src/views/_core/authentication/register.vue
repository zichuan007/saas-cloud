<script lang="ts" setup>
import type {VbenFormSchema} from '@vben/common-ui';
import {AuthenticationRegister, z} from '@vben/common-ui';
import type {Recordable} from '@vben/types';

import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {useAccessStore} from '@vben/stores';

import {notification} from 'ant-design-vue';

import {registerApi} from '#/api';

defineOptions({ name: 'Register' });

const loading = ref(false);
const router = useRouter();
const accessStore = useAccessStore();

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: '请输入企业/组织名称',
      },
      fieldName: 'tenantName',
      label: '企业名称',
      rules: z
        .string()
        .min(2, { message: '企业名称至少2个字符' })
        .max(50, { message: '企业名称最多50个字符' }),
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: '请输入联系人姓名',
      },
      fieldName: 'contactPerson',
      label: '联系人',
      rules: z.string().min(1, { message: '请输入联系人姓名' }),
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: '请输入手机号码',
      },
      fieldName: 'phone',
      label: '手机号',
      rules: z
        .string()
        .regex(/^1[3-9]\d{9}$/, { message: '请输入正确的手机号码' }),
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: '请输入短信验证码',
      },
      fieldName: 'verifyCode',
      label: '验证码',
      rules: z
        .string()
        .length(6, { message: '验证码为6位数字' })
        .regex(/^\d{6}$/, { message: '验证码为6位数字' }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: '请设置登录密码',
      },
      fieldName: 'password',
      label: '登录密码',
      rules: z
        .string()
        .min(6, { message: '密码至少6位' })
        .max(20, { message: '密码最多20位' }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: '请再次输入密码',
      },
      dependencies: {
        rules(values) {
          const { password } = values;
          return z
            .string({ required_error: '请再次输入密码' })
            .min(1, { message: '请再次输入密码' })
            .refine((value) => value === password, {
              message: '两次密码输入不一致',
            });
        },
        triggerFields: ['password'],
      },
      fieldName: 'confirmPassword',
      label: '确认密码',
    },
  ];
});

async function handleSubmit(values: Recordable<any>) {
  try {
    loading.value = true;
    const result = await registerApi({
      contactPerson: values.contactPerson,
      password: values.password,
      phone: values.phone,
      tenantName: values.tenantName,
      verifyCode: values.verifyCode,
    });

    accessStore.setAccessToken(result.accessToken);
    localStorage.setItem('refreshToken', result.refreshToken);

    notification.success({
      description: `租户编码: ${result.tenantCode}，请牢记此编码用于登录`,
      duration: 10,
      message: '注册成功',
    });

    await router.push('/');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AuthenticationRegister
    :form-schema="formSchema"
    :loading="loading"
    @submit="handleSubmit"
  />
</template>

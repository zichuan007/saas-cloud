<script lang="ts" setup>
import {onBeforeUnmount, onMounted, ref} from 'vue';

import {useRoute, useRouter} from 'vue-router';

import {Button, message, Space, Spin, Upload} from 'ant-design-vue';

import BpmnModeler from 'bpmn-js/lib/Modeler';

import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule,
} from 'bpmn-js-properties-panel';

import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';

import {
  deployDefinition,
  getBpmnXml,
  getDefinitionDetail,
  saveBpmnDraft,
} from '#/api/workflow/definition';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import '@bpmn-io/properties-panel/dist/assets/properties-panel.css';

defineOptions({ name: 'WorkflowDesigner' });

const route = useRoute();
const router = useRouter();
const definitionId = Number(route.params.id);
const processKey = ref('');
const processName = ref('');
const loading = ref(false);
const saving = ref(false);
const deploying = ref(false);

const canvasRef = ref<HTMLDivElement>();
const propertiesPanelRef = ref<HTMLDivElement>();
let modeler: InstanceType<typeof BpmnModeler> | null = null;

const defaultXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
  targetNamespace="http://bpmn.io/schema/bpmn"
  id="Definitions_1">
  <process id="Process_1" isExecutable="true">
    <startEvent id="StartEvent_1" name="开始"/>
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_1" bpmnElement="StartEvent_1">
        <dc:Bounds x="180" y="240" width="36" height="36"/>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`;

function syncProcessId(xml: string, key: string, name: string): string {
  // 匹配 <process id="xxx" 并替换为定义表的 processKey
  const processIdMatch = xml.match(/<process\s+id="([^"]+)"/);
  if (processIdMatch && processIdMatch[1]) {
    const oldId = processIdMatch[1];
    if (oldId !== key) {
      // 替换 process id 和 BPMNPlane 中的 bpmnElement 引用
      xml = xml.split(`"${oldId}"`).join(`"${key}"`);
    }
  }
  // 同步 process name
  if (name) {
    xml = xml.replace(
      /<process\s+id="[^"]*"([^>]*?)(\s+name="[^"]*")?/,
      (match, before) => {
        const cleaned = before.replace(/\s+name="[^"]*"/, '');
        return `<process id="${key}"${cleaned} name="${name}"`;
      },
    );
  }
  return xml;
}

async function getCurrentXml(): Promise<string> {
  if (!modeler) return '';
  const result = await modeler.saveXML({ format: true });
  return result.xml || '';
}

async function handleSave() {
  if (!definitionId) return;
  saving.value = true;
  try {
    const xml = await getCurrentXml();
    await saveBpmnDraft(definitionId, xml);
    message.success('草稿保存成功');
  } catch (e: any) {
    message.error('保存失败: ' + (e.message || e));
  } finally {
    saving.value = false;
  }
}

async function handleDeploy() {
  if (!definitionId) return;
  deploying.value = true;
  try {
    const xml = await getCurrentXml();
    await deployDefinition(definitionId, xml);
    message.success('部署成功');
  } catch (e: any) {
    message.error('部署失败: ' + (e.message || e));
  } finally {
    deploying.value = false;
  }
}

async function handleDownloadXml() {
  const xml = await getCurrentXml();
  if (!xml) return;
  const blob = new Blob([xml], { type: 'application/xml' });
  downloadBlob(blob, `${processName.value || 'process'}.bpmn20.xml`);
}

async function handleDownloadSvg() {
  if (!modeler) return;
  const result = await modeler.saveSVG();
  if (!result.svg) return;
  const blob = new Blob([result.svg], { type: 'image/svg+xml' });
  downloadBlob(blob, `${processName.value || 'process'}.svg`);
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

function handleImportXml(file: File) {
  const reader = new FileReader();
  reader.onload = async (e) => {
    const xml = e.target?.result as string;
    if (!xml || !modeler) return;
    try {
      await modeler.importXML(xml);
      (modeler.get('canvas') as any).zoom('fit-viewport');
      message.success('导入成功');
    } catch {
      message.error('文件格式错误，不是合法的 BPMN XML');
    }
  };
  reader.readAsText(file);
  return false;
}

function handleBack() {
  router.back();
}

onMounted(async () => {
  if (!canvasRef.value || !propertiesPanelRef.value) return;

  modeler = new BpmnModeler({
    container: canvasRef.value,
    propertiesPanel: {
      parent: propertiesPanelRef.value,
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      CamundaPlatformPropertiesProviderModule,
    ],
    moddleExtensions: {
      camunda: camundaModdleDescriptor,
    },
    keyboard: { bindTo: document },
  });

  loading.value = true;
  try {
    let xml = defaultXml;

    if (definitionId) {
      const detail = await getDefinitionDetail(definitionId);
      processKey.value = (detail as any)?.processKey || '';
      processName.value = (detail as any)?.processName || '';

      try {
        const savedXml = await getBpmnXml(definitionId);
        if (savedXml) {
          xml = savedXml as string;
        }
      } catch {
        // 没有已保存的 XML，使用默认模板
      }
    }

    // 将 XML 中 <process> 的 id 同步为定义表的 processKey，保证部署时流程标识一致
    if (processKey.value) {
      xml = syncProcessId(xml, processKey.value, processName.value);
    }

    try {
      await modeler.importXML(xml);
    } catch {
      let fallback = defaultXml;
      if (processKey.value) {
        fallback = syncProcessId(fallback, processKey.value, processName.value);
      }
      message.warning('已保存的流程图格式异常，已加载空白模板');
      await modeler.importXML(fallback);
    }

    (modeler.get('canvas') as any).zoom('fit-viewport');
  } catch (e: any) {
    message.error('加载流程图失败: ' + (e.message || e));
  } finally {
    loading.value = false;
  }
});

onBeforeUnmount(() => {
  modeler?.destroy();
  modeler = null;
});
</script>

<template>
  <div class="flex h-full flex-col">
    <div
      class="flex items-center justify-between border-b bg-white px-4 py-2"
    >
      <div class="flex items-center gap-2">
        <h3 class="m-0 text-base font-medium">
          流程设计器{{ processName ? ` - ${processName}` : '' }}
        </h3>
      </div>
      <Space>
        <Button @click="handleBack">返回</Button>
        <Upload
          :before-upload="handleImportXml"
          :show-upload-list="false"
          accept=".xml,.bpmn,.bpmn20.xml"
        >
          <Button type="default">导入 XML</Button>
        </Upload>
        <Button :loading="saving" type="default" @click="handleSave">
          保存
        </Button>
        <Button :loading="deploying" type="primary" @click="handleDeploy">
          保存并部署
        </Button>
        <Button type="default" @click="handleDownloadXml">下载 XML</Button>
        <Button type="default" @click="handleDownloadSvg">下载 SVG</Button>
      </Space>
    </div>
    <div class="relative flex flex-1 overflow-hidden">
      <Spin v-if="loading" class="absolute inset-0 z-10 flex items-center justify-center" />
      <div ref="canvasRef" class="h-full flex-1" />
      <div ref="propertiesPanelRef" class="properties-panel-container" />
    </div>
  </div>
</template>

<style>
.bjs-powered-by {
  display: none !important;
}

.properties-panel-container {
  width: 360px;
  height: 100%;
  overflow-y: auto;
  border-left: 1px solid #e8e8e8;
  background: #f7f7f8;
}

.properties-panel-container .bio-properties-panel {
  font-size: 13px;
}
</style>

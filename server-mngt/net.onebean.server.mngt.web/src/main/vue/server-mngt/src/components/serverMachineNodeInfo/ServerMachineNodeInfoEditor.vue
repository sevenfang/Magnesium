<style>
</style>


<template>
  <div>
    <Row>
      <i-col span="24"
        class="bread-crumb">
        <Breadcrumb>
          <BreadcrumbItem v-for="(item,index) in breadcrumbList"
            :to="item.path"
            :key="index">
            <Icon :type="item.icon"></Icon> {{item.name}}
          </BreadcrumbItem>
        </Breadcrumb>
      </i-col>

    </Row>
    <Divider />
    <Row>
      <i-col span="12"
        offset="6">
        <Form ref="serverMachineNodeInfoFrom"
          :model="serverMachineNodeInfoFrom"
          :rules="fromValidate"
          :label-width="170">

          <FormItem label="节点内网IP地址"
            prop="ipAddress">
            <i-input v-model="serverMachineNodeInfoFrom.ipAddress"
              placeholder="请输入节点内网IP地址"></i-input>
          </FormItem>

          <FormItem label="所在服务器访问端口号"
            prop="accessPort">
            <i-input v-model="serverMachineNodeInfoFrom.accessPort"
              placeholder="请输入访问端口号"></i-input>
          </FormItem>

          <FormItem label="访问账户"
            prop="accessUser">
            <i-input v-model="serverMachineNodeInfoFrom.accessUser"
              placeholder="请输入访问账户"></i-input>
          </FormItem>

          <FormItem label="访问授权方式"
            prop="accessAuthType">
            <Select v-model="serverMachineNodeInfoFrom.accessAuthType"
              placeholder="选择访问授权方式">
              <Option v-for="item in accessAuthTypeEunmArr"
                :value="item.value"
                :disabled="item.disabled"
                :key="item.value">{{ item.label }}</Option>
            </Select>
          </FormItem>

          <FormItem label="访问密码"
            :rules="accessPasswordValidateRules()"
            v-show="showOnPassword()"
            prop="accessPassword">
            <i-input v-model="serverMachineNodeInfoFrom.accessPassword"
              type="password"
              :disabled="serverMachineNodeInfoFrom.accessAuthType != accessAuthTypeEunmArr[0].value"
              placeholder="请输入访问密码"></i-input>
          </FormItem>

          <FormItem label="私钥相对地址"
            :rules="accessRsaPathValidateRules()"
            v-show="showOnRsa()"
            prop="accessRsaPath">
            <i-input v-model="serverMachineNodeInfoFrom.accessRsaPath"
              :disabled="serverMachineNodeInfoFrom.accessAuthType != accessAuthTypeEunmArr[1].value"
              placeholder="私钥相对地址"></i-input>
          </FormItem>

          <FormItem label="服务节点类型"
            prop="serverMachineType">
            <Select v-model="serverMachineNodeInfoFrom.serverMachineType"
              placeholder="选择服务节点类型">
              <Option v-for="item in serverMachineTypeEunmArr"
                :value="item.value"
                :disabled="item.disabled"
                :key="item.value">{{ item.label }}</Option>
            </Select>
          </FormItem>

          <FormItem>
            <Button type="primary"
              :disabled="globalButtonLoding"
              @click="handleSubmit('serverMachineNodeInfoFrom')">提交</Button>
          </FormItem>
        </Form>
      </i-col>
    </Row>
    <Spin size="large"
      fix
      v-if="globalScreenLoding" />
  </div>

</template>


<script>
export default {
  props: {
    query: { userId: 0 }
  },
  data() {
    return {
      routerPath: this.$route.path,
      serverMachineTypeEunmArr: [
        {
          value: '0',
          label: 'openresty'
        },
        {
          value: '1',
          label: 'kubernetes-master'
        }
      ],
      accessAuthTypeEunmArr: [
        {
          value: '0',
          label: '密码模式'
        },
        {
          value: '1',
          label: '公私钥模式'
        }
      ],
      serverMachineNodeInfoFrom: {
        id: this.$route.params.id,
        serverId: '',
        apiName: ''
      },
      fromValidate: {
        serverMachineType: [
          {
            required: true,
            message: '服务节点类型不能为空',
            trigger: 'change'
          }
        ],
        ipAddress: [
          {
            required: true,
            pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
            message: 'ip地址格式输入不正确',
            trigger: 'blur'
          }
        ],
        accessPort: [
          {
            required: true,
            pattern: /^[0-9]{2,6}$/,
            message: '所在服务器访问端口号输入不正确',
            trigger: 'blur'
          }
        ],
        accessUser: [
          {
            required: true,
            max: 20,
            pattern: /^[a-zA-Z]+$/,
            message: '访问账户输入不能为空,只能为英文,最大20个字符',
            trigger: 'blur'
          }
        ],
        accessAuthType: [
          {
            required: true,
            message: '访问授权方式不能为空',
            trigger: 'change'
          }
        ],
        accessPassword: [
          {
            required: true,
            max: 20,
            mix: 5,
            message: '访问账户输入不能为空,最小5个字符,最大20个字符',
            trigger: 'blur'
          }
        ],
        accessRsaPath: [
          {
            required: true,
            pattern: /^\/\S*[A-Za-z0-9]+$/,
            message: '私钥相对地址不能为空,并且只能以`/`开头 英文或数字结尾',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  mounted: function() {
    this.loadData()
  },
  computed: {
    breadcrumbList: function() {
      return this.utils.routerUtil.initRouterTreeNameArr(this.routerPath)
    },
    globalScreenLoding: function() {
      return this.$store.state.globalScreenLoding
    },
    globalButtonLoding: function() {
      return this.$store.state.globalButtonLoding
    }
  },
  methods: {
    showOnOpenresty() {
      return this.serverMachineNodeInfoFrom.serverMachineType === '0'
        ? true
        : false
    },
    handleSubmit(name) {
      this.$refs[name].validate(valid => {
        if (valid) {
          this.commitData()
        } else {
          this.$Message.error('请完善表单信息!')
        }
      })
    },
    loadData() {
      this.utils.netUtil.post(
        this.$store,
        this.API_PTAH.serverMachineNodeInfoFindById,
        this.serverMachineNodeInfoFrom,
        response => {
          this.serverMachineNodeInfoFrom = response.data.datas
        }
      )
    },
    commitData() {
      this.$store.commit('statusGlobalButtonLoding')
      this.serverMachineNodeInfoFrom.id = this.$route.params.id
      this.utils.netUtil.post(
        this.$store,
        this.API_PTAH.serverMachineNodeInfoUpdate,
        this.serverMachineNodeInfoFrom,
        () => {
          this.$Message.success('提交成功!')
          this.$store.commit('statusGlobalButtonLoding')
          this.$router.push('/server-machine-node-info-list')
        },
        () => {
          this.$store.commit('statusGlobalButtonLoding')
        }
      )
    },
    accessPasswordValidateRules() {
      let tempRuls = this.fromValidate.accessPassword
      let required =
        this.serverMachineNodeInfoFrom.accessAuthType !=
        this.accessAuthTypeEunmArr[1].value
      tempRuls[0].required = required
      return tempRuls
    },
    accessRsaPathValidateRules() {
      let tempRuls = this.fromValidate.accessRsaPath
      let required =
        this.serverMachineNodeInfoFrom.accessAuthType !=
        this.accessAuthTypeEunmArr[0].value
      tempRuls[0].required = required
      return tempRuls
    },
    showOnPassword() {
      return (
        this.serverMachineNodeInfoFrom.accessAuthType ==
        this.accessAuthTypeEunmArr[0].value
      )
    },
    showOnRsa() {
      return (
        this.serverMachineNodeInfoFrom.accessAuthType ==
        this.accessAuthTypeEunmArr[1].value
      )
    }
  }
}
</script>

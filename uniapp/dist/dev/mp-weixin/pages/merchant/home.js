"use strict";
const common_vendor = require("../../common/vendor.js");
const api_generator = require("../../api/generator.js");
const api_order = require("../../api/order.js");
const utils_image = require("../../utils/image.js");
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const user = common_vendor.ref({});
    const activeTab = common_vendor.ref("devices");
    const devices = common_vendor.ref([]);
    const orders = common_vendor.ref([]);
    const loadUser = () => {
      const u = common_vendor.index.getStorageSync("user");
      if (u) {
        try {
          user.value = JSON.parse(u);
        } catch (e) {
          user.value = u;
        }
      }
    };
    const loadDevices = async () => {
      if (!user.value.userid) return;
      try {
        const res = await api_generator.getMyGenerators(user.value.userid);
        devices.value = res || [];
      } catch (error) {
        console.error(error);
      }
    };
    const loadOrders = async () => {
      if (!user.value.userid) return;
      try {
        const res = await api_order.getMerchantOrders(user.value.userid);
        orders.value = res || [];
      } catch (error) {
        console.error(error);
      }
    };
    const goToEdit = (id) => {
      const url = id ? `/pages/merchant/generator-edit?id=${id}` : "/pages/merchant/generator-edit";
      common_vendor.index.navigateTo({ url });
    };
    const handleDelete = (id) => {
      common_vendor.index.showModal({
        title: "确认",
        content: "确定要下架该设备吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              await api_generator.deleteGenerator(id);
              common_vendor.index.showToast({ title: "已下架" });
              loadDevices();
            } catch (e) {
              common_vendor.index.showToast({ title: "操作失败", icon: "none" });
            }
          }
        }
      });
    };
    const goToOrder = (id) => {
      common_vendor.index.navigateTo({ url: `/pages/order/detail?id=${id}` });
    };
    const formatStockStatus = (s) => {
      const map = { "AVAILABLE": "可租", "RENTED": "已租", "MAINTENANCE": "维护中" };
      return map[s] || s;
    };
    const formatStatus = (s) => {
      const map = {
        "WAIT_CONFIRM": "待确认",
        "CONFIRMED": "已确认",
        "DELIVERED": "交付中",
        "RENTING": "租赁中",
        "WAIT_RETURN": "待归还",
        "COMPLETED": "已完成",
        "CANCELLED": "已取消"
      };
      return map[s] || s;
    };
    const formatDate = (d) => {
      if (!d) return "";
      return new Date(d).toLocaleDateString();
    };
    common_vendor.onMounted(() => {
      loadUser();
      loadDevices();
      loadOrders();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: user.value.name
      }, user.value.name ? {
        b: common_vendor.t(user.value.name)
      } : {}, {
        c: common_vendor.n(activeTab.value === "devices" ? "active" : ""),
        d: common_vendor.o(($event) => activeTab.value = "devices"),
        e: common_vendor.n(activeTab.value === "orders" ? "active" : ""),
        f: common_vendor.o(($event) => activeTab.value = "orders"),
        g: activeTab.value === "devices"
      }, activeTab.value === "devices" ? common_vendor.e({
        h: common_vendor.o(($event) => goToEdit(null)),
        i: common_vendor.f(devices.value, (item, k0, i0) => {
          return {
            a: common_vendor.unref(utils_image.getImageUrl)(item.imageUrl ? item.imageUrl.split(",")[0] : ""),
            b: common_vendor.t(item.name),
            c: common_vendor.t(formatStockStatus(item.stockStatus)),
            d: common_vendor.n(item.stockStatus),
            e: common_vendor.t(item.power),
            f: common_vendor.t(item.dailyRent),
            g: common_vendor.o(($event) => goToEdit(item.id), item.id),
            h: common_vendor.o(($event) => handleDelete(item.id), item.id),
            i: item.id
          };
        }),
        j: devices.value.length === 0
      }, devices.value.length === 0 ? {} : {}) : {}, {
        k: activeTab.value === "orders"
      }, activeTab.value === "orders" ? common_vendor.e({
        l: common_vendor.f(orders.value, (order, k0, i0) => {
          return {
            a: common_vendor.t(order.id),
            b: common_vendor.t(formatStatus(order.status)),
            c: common_vendor.t(order.generatorName),
            d: common_vendor.t(order.totalAmount),
            e: common_vendor.t(formatDate(order.createdAt)),
            f: order.id,
            g: common_vendor.o(($event) => goToOrder(order.id), order.id)
          };
        }),
        m: orders.value.length === 0
      }, orders.value.length === 0 ? {} : {}) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d43dddfc"]]);
wx.createPage(MiniProgramPage);

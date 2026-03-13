"use strict";
const common_vendor = require("../../common/vendor.js");
const api_generator = require("../../api/generator.js");
const utils_image = require("../../utils/image.js");
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const user = common_vendor.ref({});
    const loading = common_vendor.ref(false);
    const generators = common_vendor.ref([]);
    const powerOptions = ["全部", "50KW", "100KW", "200KW", "500KW"];
    const powerIndex = common_vendor.ref(0);
    const searchForm = common_vendor.reactive({
      power: "",
      location: "",
      minRent: void 0,
      maxRent: void 0,
      sortType: "",
      userLatitude: null,
      userLongitude: null
    });
    const loadUser = () => {
      const storedUser = common_vendor.index.getStorageSync("user");
      if (storedUser) {
        try {
          user.value = JSON.parse(storedUser);
        } catch (e) {
          user.value = storedUser;
        }
      }
    };
    const bindPowerChange = (e) => {
      const idx = e.detail.value;
      powerIndex.value = idx;
      searchForm.power = powerOptions[idx] === "全部" ? "" : powerOptions[idx];
      handleSearch();
    };
    const toggleSort = (type) => {
      if (type === "price") {
        searchForm.sortType = searchForm.sortType === "price_asc" ? "price_desc" : "price_asc";
      } else if (type === "distance") {
        searchForm.sortType = "distance";
        searchForm.userLatitude = 39.9042;
        searchForm.userLongitude = 116.4074;
      }
      handleSearch();
    };
    const handleSearch = async () => {
      loading.value = true;
      try {
        const response = await api_generator.searchGenerators(searchForm);
        generators.value = response || [];
      } catch (error) {
        common_vendor.index.showToast({ title: "获取列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const formatDistance = (km) => {
      if (km < 1) return "< 1km";
      return km.toFixed(1) + "km";
    };
    const goToDetail = (id) => {
      common_vendor.index.navigateTo({ url: `/pages/generator/detail?id=${id}` });
    };
    const goToProfile = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/profile" });
    };
    const goToCart = () => {
      common_vendor.index.navigateTo({ url: "/pages/cart/cart" });
    };
    const goToOrders = () => {
      common_vendor.index.navigateTo({ url: "/pages/tenant/orders" });
    };
    const handleLogout = () => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.removeStorageSync("user");
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.reLaunch({ url: "/pages/login/login" });
          }
        }
      });
    };
    common_vendor.onMounted(() => {
      loadUser();
      handleSearch();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: user.value.avatar || "/static/default-avatar.png",
        b: common_vendor.o(goToProfile),
        c: user.value.username
      }, user.value.username ? {
        d: common_vendor.t(user.value.username)
      } : {}, {
        e: common_vendor.o(handleLogout),
        f: common_vendor.t(searchForm.power || "全部功率"),
        g: common_vendor.o(bindPowerChange),
        h: powerIndex.value,
        i: powerOptions,
        j: common_vendor.o(handleSearch),
        k: searchForm.location,
        l: common_vendor.o(($event) => searchForm.location = $event.detail.value),
        m: common_vendor.o(handleSearch),
        n: searchForm.minRent,
        o: common_vendor.o(($event) => searchForm.minRent = $event.detail.value),
        p: searchForm.maxRent,
        q: common_vendor.o(($event) => searchForm.maxRent = $event.detail.value),
        r: common_vendor.n(searchForm.sortType === "price_asc" ? "active" : ""),
        s: common_vendor.o(($event) => toggleSort("price")),
        t: common_vendor.n(searchForm.sortType === "distance" ? "active" : ""),
        v: common_vendor.o(($event) => toggleSort("distance")),
        w: generators.value.length === 0
      }, generators.value.length === 0 ? {} : {}, {
        x: common_vendor.f(generators.value, (gen, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.unref(utils_image.getImageUrl)(gen.imageUrl ? gen.imageUrl.split(",")[0] : ""),
            b: common_vendor.t(gen.name),
            c: common_vendor.t(gen.power),
            d: common_vendor.t(gen.address || "未知地点"),
            e: gen.distance
          }, gen.distance ? {
            f: common_vendor.t(formatDistance(gen.distance))
          } : {}, {
            g: common_vendor.t(gen.dailyRent),
            h: gen.averageRating
          }, gen.averageRating ? {
            i: common_vendor.t(Number(gen.averageRating).toFixed(1))
          } : {}, {
            j: gen.id,
            k: common_vendor.o(($event) => goToDetail(gen.id), gen.id)
          });
        }),
        y: loading.value
      }, loading.value ? {} : {}, {
        z: common_vendor.o((...args) => _ctx.loadMore && _ctx.loadMore(...args)),
        A: common_vendor.o(goToCart),
        B: common_vendor.o(goToOrders)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-156f7e2c"]]);
wx.createPage(MiniProgramPage);

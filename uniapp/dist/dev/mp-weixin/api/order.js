"use strict";
const utils_request = require("../utils/request.js");
const getMerchantOrders = (merchantId) => {
  return utils_request.service.get(`/orders/merchant/${merchantId}`);
};
exports.getMerchantOrders = getMerchantOrders;

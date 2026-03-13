const baseURL = 'http://localhost:8081/api';

const request = (options) => {
    return new Promise((resolve, reject) => {
        // Handle URL
        let url = options.url;
        if (!url.startsWith('http')) {
            url = baseURL + url;
        }

        // Handle Headers
        const header = options.header || {};
        // Content-Type default
        if (!header['content-type']) {
            header['content-type'] = 'application/json';
        }

        const token = uni.getStorageSync('token');
        if (token) {
            header['Authorization'] = 'Bearer ' + token;
        }
        const userStr = uni.getStorageSync('user');
        if (userStr) {
            try {
                // Handle if userStr is already an object (uni.setStorageSync handles objects differently than localStorage sometimes)
                // But generally setStorageSync stores as is.
                // However, the original code used JSON.stringify/parse.
                // In UniApp setStorageSync can store objects directly, but let's assume it was stored as string for compatibility if copied logic.
                // Actually, let's just check type.
                const user = typeof userStr === 'string' ? JSON.parse(userStr) : userStr;
                if (user && user.userid) {
                    header['X-User-ID'] = user.userid;
                }
            } catch (e) {
                // ignore
            }
        }

        uni.request({
            url: url,
            method: (options.method || 'GET').toUpperCase(),
            data: options.data || options.params,
            header: header,
            success: (res) => {
                // Network success (UniApp success means network connected)
                if (res.statusCode >= 200 && res.statusCode < 300) {
                     const data = res.data;
                     // Check for standard Result format
                     if (data.code !== undefined) {
                         if (data.code === 200) {
                             resolve(data.data);
                         } else {
                             uni.showToast({
                                 title: data.msg || 'Error',
                                 icon: 'none',
                                 duration: 2000
                             });
                             reject(new Error(data.msg || 'Error'));
                         }
                     } else {
                         // Fallback
                         resolve(data);
                     }
                } else {
                    let message = 'Server Error: ' + res.statusCode;
                    if (res.statusCode === 401) {
                        message = 'Unauthorized';
                        // Optional: Redirect to login
                        uni.reLaunch({ url: '/pages/login/login' });
                    }
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    });
                    reject(new Error(message));
                }
            },
            fail: (err) => {
                uni.showToast({
                    title: 'Network Error',
                    icon: 'none'
                });
                reject(err);
            }
        });
    });
};

// Emulate axios signature
const service = (options) => request(options);
service.get = (url, config) => request({ ...config, url, method: 'GET' });
service.post = (url, data, config) => request({ ...config, url, data, method: 'POST' });
service.put = (url, data, config) => request({ ...config, url, data, method: 'PUT' });
service.delete = (url, config) => request({ ...config, url, method: 'DELETE' });

export default service;

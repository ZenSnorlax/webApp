import {defineConfig} from "vite";
import vue from "@vitejs/plugin-vue";
import {resolve} from 'path';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        port: 1421,
        open: true,
    },
    resolve: {
        // 设置文件./src路径为 @
        alias: [
            {
                find: '@',
                replacement: resolve(__dirname, './src')
            }
        ]
    }

});

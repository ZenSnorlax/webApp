// Below is partial code of E:/tauri-app/snorlaxReader/src/views/HomeView.vue:
import {createRouter, createWebHistory} from "vue-router";

const routes = [
    {
        path: '/',
        name: 'home',
        component: () => import('../views/HomeView.vue')
    },
    {
        path: '/auth/login',
        name: 'login',
        component: () => import('../views/LoginView.vue')
    },
    {
        path: '/auth/register',
        name: 'register',
        component: () => import('../views/RegisterView.vue')
    },
    {
        path: '/book/upload',
        name: 'upBook',
        component: () => import('../views/UpBookView.vue')
    },
    {
        path:'/book/reader',
        name:'reader',
        component: () => import('../views/ReaderView.vue')
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Index from '../views/Index.vue'
import User from '../views/sys/User.vue'
import Role from '../views/sys/Role.vue'
import Menu from '../views/sys/Menu.vue'

import axios from '../axios'
import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    children: [
      {
        path: '/index',
        name: 'Index',
        component: Index
      },
      {
        path: '/userCenter',
        name: 'UserCenter',
        component: () => import('../views/UserCenter.vue'),
        meta: {
          icon: '',
          title: '个人中心'
        }
      },
      // {
      //   path: '/sys/users',
      //   name: 'SysUser',
      //   component: User
      // },
      // {
      //   path: '/sys/roles',
      //   name: 'SysRole',
      //   component: Role
      // },
      // {
      //   path: '/sys/menus',
      //   name: 'SysMenu',
      //   component: Menu
      // }
    ]
  },

  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  }
]

const router = new VueRouter({
  routes,
  mode: 'history'
})

router.beforeEach((to, from, next) => {

  let token = localStorage.getItem('token')
  let hasRoute = store.state.menus.hasRoute;

  if (to.path === '/login') {
    next()
  } else if (!token) {
    next({path: '/login'})
  } else if (token && !hasRoute) {
    axios.get('/sys/menus/nav').then(res => {
      // 拿到menuList
      store.commit('setMenuList', res.data.data.nav);
      // 拿到用户权限
      store.commit('setPermList', res.data.data.authoritys);

      // 动态绑定路由
      let newRoutes = router.options.routes;

      res.data.data.nav.forEach(menu => {
        if (menu.children) {
          menu.children.forEach(e => {

            // 转化路由
            let route = menuToRoute(e);
            // 把路由添加到路由管理器中
            if (route) {
              newRoutes[0].children.push(route);
            }
          })
        }
      });
      router.addRoutes(newRoutes);

      hasRoute = true;
      store.commit('changeRouteStatus', hasRoute);
    })
  }



  next();
})

// 导航转成路由
const menuToRoute = (menu) => {
  if(!menu.component) {
    return null
  }

  let route = {
    name: menu.name,
    path: menu.path,
    meta: {
      icon: menu.icon,
      title: menu.title
    }
  };
  route.component = () => import('@/views/' + menu.component + '.vue');

  return route;
}

export default router

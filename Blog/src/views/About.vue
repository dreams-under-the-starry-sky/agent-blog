<script setup lang="ts">
import { usePageReadyOnMount } from '@/utils/pageReady'

usePageReadyOnMount()
</script>

<template>
  <div class="about">
    <header class="about-title">关于</header>

    <section>
      <h2 class="section-title">🚩 关于我</h2>
      <ul>
        <li><b>昵称：</b>Crossroads</li>
        <li><b>邮箱：</b>1762546812@qq.com</li>
        <li><b>为什么取这个名字：</b>人生就是无数个十字路口，别想太多，选一个看得顺眼的就行 🤪</li>
      </ul>
    </section>

    <section>
      <h2 class="section-title">🔨 关于本站</h2>
      <ul>
        <li>
          <b>后端：</b>
          <mark><a href="https://spring.io/projects/spring-boot" target="_blank" rel="noreferrer">SpringBoot3</a></mark>
          +
          <mark><a href="https://mybatis.org/mybatis-3/" target="_blank" rel="noreferrer">Mybatis</a></mark>
          +
          <mark><a href="https://dev.mysql.com/doc/" target="_blank" rel="noreferrer">MySQL8</a></mark>
        </li>
        <li>
          <b>前端：</b>
          <mark><a href="https://cn.vuejs.org/" target="_blank" rel="noreferrer">Vue3</a></mark>
          +
          <mark><a href="https://element-plus.org/" target="_blank" rel="noreferrer">ElementPlus</a></mark>
        </li>
        <li>
          <b>云服务：</b>
          <mark><a href="https://cloud.tencent.com/" target="_blank" rel="noreferrer">腾讯全家桶（服务器+域名+COS+CDN）</a></mark>
        </li>
      </ul>
    </section>

    <section>
      <h2 class="section-title">🎈 业余爱好</h2>
      <ul>
        <li>骑自行车 🚲 、打羽毛球 🏸</li>
        <li>喜欢干饭 🍚 与旅行 🎒</li>
        <li>正在学习剪视频 🎬、拍照 📸（脑子一热买了相机与pocket3🤣）</li>
        <li>爱听音乐 🎧，偶尔打游戏（虽然很菜）🎮</li>
        <li>
          精通Java、JavaScript、HTML、CSS、php、Go等语言的拼写，精通SSH、SSM等主流框架的缩写，精通Oracle、MySQL数据库的删库跑路，熟练掌握windows、Linux 操作系统的开关机 😈 总之人和代码一个能跑就行 🏃‍♂️
        </li>
      </ul>
    </section>

    <section>
      <h2 class="section-title">🙋‍♂️ 声明</h2>
      <ul>
        <li>
          本站以分享互联网经验、学习知识为目的，所有文章所涉及使用的工具、资源等均来自互联网， 仅供学习和研究使用，版权归作者所有，如果无意之中侵犯了您的版权，请来信告知。本站将在第一时间删除！另外，本站内的文章多为博主原创，大部分是由CSDN平台搬迁过来，仅供学习交流之用，不参与商业用途。
        </li>
        <li>转载请注明文章地址及作者</li>
      </ul>
    </section>
  </div>
</template>

<style scoped lang="scss">
.about {
  line-height: 1.85;
}

.about-title {
  margin: 0 0 1.5rem;
  text-align: left;
  font-size: calc(2.2rem + 2px);
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--c-text-1);
}

.section-title {
  margin: 1.4rem 0 0.7rem;
  font-size: calc(1.15rem + 2px);
  font-weight: 700;
  color: var(--btn-content);
}

ul {
  margin: 0;
  padding-left: 1.6rem;
  color: var(--c-text-1);
}

li {
  margin: 0.35rem 0;
  list-style: circle;
}

li::marker {
  color: var(--primary);
}

mark {
  position: relative;
  display: inline-block;
  color: var(--c-text-1);
  background: transparent;
}

mark a {
  color: var(--btn-content);
}

mark::before,
mark::after {
  content: "";
  position: absolute;
  bottom: 0;
  width: 0;
  height: 2px;
  background: var(--primary);
  transition: width 0.4s ease;
}

mark::before {
  left: 0;
}

mark::after {
  right: 0;
}

mark:hover::before,
mark:hover::after {
  width: 100%;
}
</style>

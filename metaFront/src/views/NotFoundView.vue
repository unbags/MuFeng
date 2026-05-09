<template>
  <div class="not-found-page">
    <h1>404</h1>
    <p>页面未找到</p>
    <router-link to="/menu">返回菜单</router-link>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

h1 {
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(72px, 10vw, 140px);
  font-weight: 400;
  color: var(--accent);
  margin: 0;
}

p {
  color: var(--muted);
  font-size: 18px;
  margin: 16px 0 32px;
}

a {
  display: inline-flex;
  min-height: 48px;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  border: 1px solid var(--ink);
  background: var(--ink);
  color: #fff;
  font-weight: 800;
  text-decoration: none;
  transition: transform 520ms var(--spring);
}

a:hover {
  transform: translateY(-5px) scale(1.012);
}
</style>
